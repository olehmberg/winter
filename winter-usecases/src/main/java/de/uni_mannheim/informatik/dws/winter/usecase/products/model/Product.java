/*
 * Copyright (c) 2017 Data and Web Science Group, University of Mannheim, Germany (http://dws.informatik.uni-mannheim.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package de.uni_mannheim.informatik.dws.winter.usecase.products.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;

/**
 * A {@link AbstractRecord} representing a product.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class Product extends AbstractRecord<Attribute> implements Serializable {

	/*
	 * example entry
	 * 14219585,Computers_and_Accessories,,"Apple Mac Pro MD878LL/A Desktop: Designed for professional product editing,
	 * graphics work and multiple Ultra HD 4K displays, this impressive computer provides top-tier power and connectivity.
	 * The eye-catching body provides efficient, quiet cooling and fits easily on a desk.@en-US",,
	 * Apple - Mac Pro Desktop Computer 6-Core Intel&reg; Xeon&reg; Processor 16GB Memory 256GB Flash Storage Black@en-US
	 */

	private static final long serialVersionUID = 1L;

	public Product(String identifier, String provenance) {
		super(identifier, provenance);
	}

	private String title;
	private String category;
	private String brand;
	private String description;
	private String price;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	private final Map<Attribute, Collection<String>> provenance = new HashMap<>();
	private Collection<String> recordProvenance;

	public void setRecordProvenance(Collection<String> provenance) {
		recordProvenance = provenance;
	}

	public Collection<String> getRecordProvenance() {
		return recordProvenance;
	}

	public void setAttributeProvenance(Attribute attribute,
			Collection<String> provenance) {
		this.provenance.put(attribute, provenance);
	}

	public Collection<String> getAttributeProvenance(String attribute) {
		return provenance.get(attribute);
	}

	public String getMergedAttributeProvenance(Attribute attribute) {
		Collection<String> prov = provenance.get(attribute);

		if (prov != null) {
			return StringUtils.join(prov, "+");
		} else {
			return "";
		}
	}

	public static final Attribute TITLE = new Attribute("Title");
	public static final Attribute CATEGORY = new Attribute("Category");
	public static final Attribute BRAND = new Attribute("Brand");
	public static final Attribute DESCRIPTION = new Attribute("Description");
	public static final Attribute PRICE = new Attribute("Price");
	
	@Override
	public boolean hasValue(Attribute attribute) {
		if(attribute==TITLE)
			return getTitle() != null && !getTitle().isEmpty();
		else if(attribute==CATEGORY)
			return getCategory() != null && !getCategory().isEmpty();
		else if(attribute==BRAND)
			return getBrand() != null && !getBrand().isEmpty();
		else if(attribute==DESCRIPTION)
			return getDescription() != null && getDescription().isEmpty();
		else if(attribute==PRICE)
			return getPrice() != null && getPrice().isEmpty();
		else
			return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Product product = (Product) o;
		return title.equals(product.title) &&
				category.equals(product.category) &&
				brand.equals(product.brand) &&
				description.equals(product.description) &&
				price.equals(product.price);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), title, category, brand, description, price);
	}

	@Override
	public String toString() {
		return "Product{" +
				"title='" + title + '\'' +
				", category='" + category + '\'' +
				", brand='" + brand + '\'' +
				", description='" + description + '\'' +
				", price='" + price + '\'' +
				", provenance=" + provenance +
				", recordProvenance=" + recordProvenance +
				'}';
	}
}
