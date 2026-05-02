package com.navercorp.fixturemonkey.docs.getstarted;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.navercorp.fixturemonkey.FixtureMonkey;
import org.junit.jupiter.api.Test;

class WithoutLombokTest {

	public static class Product {
		private long id;
		private String productName;
		private long price;
		private List<String> options;
		private Instant createdAt;
		private ProductType productType;
		private Map<Integer, String> merchantInfo;

		public Product() {
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public long getPrice() {
			return price;
		}

		public void setPrice(long price) {
			this.price = price;
		}

		public List<String> getOptions() {
			return options;
		}

		public void setOptions(List<String> options) {
			this.options = options;
		}

		public Instant getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(Instant createdAt) {
			this.createdAt = createdAt;
		}

		public ProductType getProductType() {
			return productType;
		}

		public void setProductType(ProductType productType) {
			this.productType = productType;
		}

		public Map<Integer, String> getMerchantInfo() {
			return merchantInfo;
		}

		public void setMerchantInfo(Map<Integer, String> merchantInfo) {
			this.merchantInfo = merchantInfo;
		}
	}

	public enum ProductType {
		ELECTRONICS,
		CLOTHING,
		FOOD
	}

	@Test
	void test() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.create();

		// when
		Product actual = fixtureMonkey.giveMeOne(Product.class);

		// then
		then(actual).isNotNull();
	}
}
