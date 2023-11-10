package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.GtwOperationEnum;

public class TransactionConverter {

    private final Object data;
    private final GtwOperationEnum op;

    public TransactionConverter(Object data, GtwOperationEnum op) {
        this.data = data;
        this.op = op;
    }

    public String convert() {
        String out = null;
        switch (op) {
            case CREATE:
            case REPLACE:
                out = toTransaction((String) data);
                break;
            case UPDATE:
            case DELETE:
                throw new IllegalArgumentException("Unsupported operation for type document " + op.name());
        }
        return out;
    }

    public String toTransaction(String transaction) {
        return transaction;
    }
}
