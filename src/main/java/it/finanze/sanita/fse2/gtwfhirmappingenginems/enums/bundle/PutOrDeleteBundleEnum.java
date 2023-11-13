package it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.bundle;

public enum PutOrDeleteBundleEnum {
	MESSAGE;
	public BundleTypeEnum toGeneric() {
		return BundleTypeEnum.valueOf(name());
	}
}
