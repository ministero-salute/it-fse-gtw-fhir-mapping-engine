# FSE 2.0 — FHIR Mapping Engine

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

An FSE 2.0 Gateway microservice that transforms HL7 CDA documents into FHIR R4
Bundles using the [FHIR Mapping
Language](https://build.fhir.org/mapping-language.html).

## Features

The service:

- loads mapping engines and their FHIR resources (`StructureMap`,
  `StructureDefinition`, and `ValueSet`) from MongoDB;
- transforms CDA documents into FHIR R4 Bundles using Matchbox;
- normalizes the generated Bundle, updates references, and removes duplicate
  resources according to the configured strategy;
- creates or updates the `DocumentReference` resource with the supplied
  metadata;
- optionally performs FHIR R4 validation and reference traversability checks;
- converts `document` Bundles into `transaction` Bundles;
- provides APIs for inspecting and refreshing the available mapping engines.

> **Note:** transformation maps are not defined in the application source code.
> They must be stored in MongoDB together with the engine configuration that
> uses them.

## Processing stages and scope

The transformation endpoints follow this order:

```text
CDA XML
  -> Matchbox mapping
  -> Bundle post-processing (including the optional removal report)
  -> FHIR Bundle JSON serialization
  -> Optional checks of the final JSON (JSON endpoint only)
  -> Response
```

| Stage | Purpose | Changes the returned Bundle? | Control / result |
| --- | --- | --- | --- |
| Mapping | Convert CDA into FHIR resources using configured maps | Creates the Bundle | `engineId` and `objectId` |
| [Post-processing](#bundle-post-processing) | Normalize IDs/references, deduplicate, enrich metadata, and clean internal tags | Yes | Runs on both transformation endpoints |
| [Resource removal report](#resource-removal-report--risorse-soppresse) | Move removal warnings into the document narrative | Yes, when report preparation succeeds | `includeRemovalReport`; diagnostics in `normalizationIssues` on the JSON endpoint |
| [Normative R4 validation](#normative-r4-validation) | Check core FHIR R4 conformance | No | `validations=NORMATIVE_R4`; findings in `validationResponse` |
| [Reference traversability](#reference-traversability) | Check graph reachability from the first DocumentReference root | No | `validations=TRAVERSABILITY`; findings in `validationResponse` |

**Post-processing is not validation.** Updating references during normalization
does not perform the traversability check. Conversely, traversability reports
unreachable resources/references; it does not repair links or remove resources.
Both optional checks inspect the final post-processed JSON, not the raw mapping
output, and neither changes the returned Bundle.

Standalone metadata updates and document-to-transaction conversion are separate
operations; they do not run this full transformation workflow.

## Configuration

Common values are defined in `src/main/resources/application.properties`. The
`dev` and `docker` profiles override them through `application-dev.properties`
and `application-docker.properties`, respectively.

The `fhir.transform.alg` property selects the
[post-processing deduplication strategy](#deduplication-strategies).

## Main APIs

| Method    | Endpoint                                                  | Description                       |
| --------- | --------------------------------------------------------- | --------------------------------- |
| `POST`    | `/v1/documents/transform`                                 | Transforms a CDA supplied in a JSON body, with optional validation |
| `POST`    | `/v1/documents/transform/stateless/{engineId}/{objectId}` | Transforms a multipart CDA file |
| `POST`    | `/v1/documents/transform/update`                          | Updates the metadata of a `DocumentReference` |
| `POST`    | `/v1/documents/transform/document-to-transaction`         | Converts a FHIR `document` Bundle into a `transaction` Bundle |
| `POST`    | `/v1/documents/transform/add-document-reference`          | Adds or updates a Bundle's `DocumentReference` |
| `GET`     | `/v1/engine/status`                                       | Lists the engines currently loaded in memory |
| `GET`     | `/v1/engine/refresh`                                      | Starts an asynchronous engine refresh |

The Swagger UI provides the complete request and response schemas.

### Example: multipart transformation

`engineId` identifies the engine configuration, while `objectId` identifies the
root map associated with the document type. Both values must match records
stored in MongoDB.

```bash
curl --request POST \
  "http://localhost:8022/v1/documents/transform/stateless/<engineId>/<objectId>" \
  --form "file=@cda-document.xml;type=application/xml"
```

An optional `documentReference` part can also be provided:

```bash
curl --request POST \
  "http://localhost:8022/v1/documents/transform/stateless/<engineId>/<objectId>" \
  --form "file=@cda-document.xml;type=application/xml" \
  --form 'documentReference={"facilityTypeCode":"Hospital","eventCode":["P99"],"serviceStartTime":"20260115103000"};type=application/json'
```

### Example: JSON transformation with validation

The `cda` field contains the XML document as a JSON string. Supported
`validations` values are `NORMATIVE_R4` and `TRAVERSABILITY`. Repeat the query
parameter to request both checks.
Omitting `validations` skips both checks but still runs Bundle post-processing.
The multipart endpoint does not expose the `validations` option.

```bash
jq -n \
  --rawfile cda cda-document.xml \
  --arg engineId '<engineId>' \
  --arg objectId '<objectId>' \
  '{cda: $cda, engineId: $engineId, objectId: $objectId}' \
| curl --request POST \
    'http://localhost:8022/v1/documents/transform?validations=NORMATIVE_R4&validations=TRAVERSABILITY' \
    --header 'Content-Type: application/json' \
    --data-binary @-
```

## Bundle post-processing

This stage prepares and modifies the mapped Bundle before serialization and
before any optional checks. It runs on both JSON and multipart transformations,
regardless of whether `NORMATIVE_R4` or `TRAVERSABILITY` is requested.

The steps run in this order:

1. Normalize resource IDs and update references.
2. Remove duplicate resources using the configured strategy.
3. Enrich the first existing `DocumentReference` when metadata is supplied;
   this step does not create a missing `DocumentReference`.
4. Remove internal scoring tags while preserving FHIR signatures.
5. Optionally include a resource removal report in the document narrative.

### Deduplication strategies

The `fhir.transform.alg` property supports:

- `KEEP_LONGER`: keeps the resource with the longest JSON representation;
- `KEEP_RICHER_UP`: keeps the resource with the most elements, preferring the
  first one in case of a tie;
- `KEEP_RICHER_DOWN`: keeps the resource with the most elements, preferring the
  last one in case of a tie;
- `KEEP_PRIOR`: uses the scoring tags generated by the mapping process.

### Resource removal report — Risorse soppresse

Resource removal reporting is transformation post-processing, not an optional
validation. Both JSON and multipart transformation endpoints accept the boolean
query parameter `includeRemovalReport`, defaulting to `false`. Omit it or set
it to `false` to skip this step; use `?includeRemovalReport=true` to enable it.
It controls narrative reporting, not resource pruning during transformation.
All other post-processing steps run regardless of this flag.

The JSON endpoint returns these diagnostics in `normalizationIssues`, independently
of optional `validationResponse` results. An empty `normalizationIssues` array
means no diagnostics were reported by post-processing; warnings do not constitute
a transformation failure. Optional validations check the final normalized JSON.
The multipart endpoint retains its bare-Bundle response and does not expose a
separate diagnostics field.

During mapping, removed resources can be described by `OperationOutcome` entries.
When reporting is enabled, their warnings are moved into a **Risorse soppresse**
block in `Composition.text`, preserving the existing narrative. After successful
report preparation, the OperationOutcome entries are removed. With no such
entries, the Bundle is unchanged.

The report does not restore removed resources or guarantee that the Bundle is
upload-ready. If the Composition is missing or its narrative cannot be used,
the report is skipped and the OperationOutcome entries remain in the Bundle.

## Optional validation and traversability checks

These checks are available through the JSON transformation endpoint's repeated
`validations` query parameter. Select either check independently or request both;
neither is a prerequisite for the other. They run after post-processing and
serialization and report findings without modifying the Bundle.

Results are returned in `validationResponse`, separately for each requested check,
with an overall validity flag. Only `ERROR` and `FATAL` findings make a result
invalid. Post-processing diagnostics belong to `normalizationIssues`, not to
`validationResponse`; an empty diagnostics array is not proof of FHIR conformance
or traversability.

### Normative R4 validation

`NORMATIVE_R4` checks core FHIR R4 resource constraints and terminology using HAPI
with standard R4 definitions and local terminology support.
`WARNING`, `ERROR`, and `FATAL` messages are returned as normative issues.
Warnings do not make the result invalid; only `ERROR` and `FATAL` do.
Informational messages are omitted.

This does not guarantee conformance to Italian implementation guides whose
definitions have not been loaded. Unknown ordinary extensions are accepted;
required terminology bindings and resource constraints are still checked.

### Reference traversability

`TRAVERSABILITY` checks graph reachability, not FHIR conformance. It builds a
directed graph of Bundle resources and references and performs depth-first
traversal from the first node whose normalized ID contains `DocumentReference`,
in Bundle order. Other DocumentReference resources are not independent roots.

Unreachable resources and references are returned as `ERROR`/`NOTFOUND` issues.
Reference paths use `source^target^referencePath`; isolated resources use their
normalized ID. With no root, the graph remains untraversed; an empty graph
produces no issues.

This is not a complete reference-integrity check: dangling references from
reachable nodes can pass, resources without IDs are excluded as graph nodes,
and UUID fullUrls are not treated as aliases for typed resource IDs.

## Related repositories

- [it-fse-support](https://github.com/ministero-salute/it-fse-support) — FSE 2.0 support
- [it-fse-landing](https://github.com/ministero-salute/it-fse-landing) — index of FSE 2.0 repositories

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before proposing changes.

## License

This project is distributed under the [GNU Affero General Public License v3.0](LICENSE).
