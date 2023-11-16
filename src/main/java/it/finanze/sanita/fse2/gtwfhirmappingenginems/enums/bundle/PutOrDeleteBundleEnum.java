package it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.bundle;

public enum PutOrDeleteBundleEnum {
	MESSAGE,
	TRANSACTION;
	public BundleTypeEnum toGeneric() {
		return BundleTypeEnum.valueOf(name());
	}
}
