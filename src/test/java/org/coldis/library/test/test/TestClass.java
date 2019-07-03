package org.coldis.library.test.test;

import java.util.Objects;

/**
 * Test class.
 */
public class TestClass implements Cloneable {

	/**
	 * Test attribute.
	 */
	private String test1;

	/**
	 * Test attribute.
	 */
	private Long test2;

	/**
	 * Test attribute.
	 */
	private TestClass test3;

	/**
	 * Default constructor.
	 *
	 * @param test1 Test attribute.
	 * @param test2 Test attribute.
	 * @param test3 Test attribute.
	 */
	public TestClass(final String test1, final Long test2, final TestClass test3) {
		super();
		this.test1 = test1;
		this.test2 = test2;
		this.test3 = test3;
	}

	/**
	 * Gets the test1.
	 *
	 * @return The test1.
	 */
	public String getTest1() {
		return this.test1;
	}

	/**
	 * Sets the test1.
	 *
	 * @param test1 New test1.
	 */
	public void setTest1(final String test1) {
		this.test1 = test1;
	}

	/**
	 * Gets the test2.
	 *
	 * @return The test2.
	 */
	public Long getTest2() {
		return this.test2;
	}

	/**
	 * Sets the test2.
	 *
	 * @param test2 New test2.
	 */
	public void setTest2(final Long test2) {
		this.test2 = test2;
	}

	/**
	 * Gets the test3.
	 *
	 * @return The test3.
	 */
	public TestClass getTest3() {
		return this.test3;
	}

	/**
	 * Sets the test3.
	 *
	 * @param test3 New test3.
	 */
	public void setTest3(final TestClass test3) {
		this.test3 = test3;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.test1, this.test2, this.test3);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TestClass)) {
			return false;
		}
		final TestClass other = (TestClass) obj;
		return Objects.equals(this.test1, other.test1) && Objects.equals(this.test2, other.test2)
				&& Objects.equals(this.test3, other.test3);
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
