package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository;

import java.io.Serializable;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.ValuesetETY;

public interface IValueSetRepo extends Serializable {

	ValuesetETY findValueSetByName(String valuesetName);
}
