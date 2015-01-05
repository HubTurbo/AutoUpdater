package htlauncher.utilities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="version")
@XmlAccessorType(XmlAccessType.FIELD)
public class Version implements Comparable<Version> {
	private int major = 0;
	private int minor = 0;
	private int patch = 0;

	public Version () {
	}
	
	public Version(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}
	
	public Version(String str) {
		// Strip everything that is not a number or dot
		str = str.replaceAll("[^0-9.]+", "");
		
		// Split by the . delimiter
		String[] temp = str.split("\\.");
		
		try {
			major = temp.length > 0 ? Integer.parseInt(temp[0]) : 0;
			minor = temp.length > 1 ? Integer.parseInt(temp[1]) : 0;
			patch = temp.length > 2 ? Integer.parseInt(temp[2]) : 0;
		} catch (NumberFormatException e) {
			// Default everything to zero
			major = 0;
			minor = 0;
			patch = 0;
		}
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public int getPatch() {
		return patch;
	}

	public void setPatch(int patch) {
		this.patch = patch;
	}

	@Override
	public int compareTo(Version other) {
		int diff = this.major - other.major;
		if (diff != 0) {
			// Major versions are different
			return diff;
		}
		// Major versions are the same; continue with minor version
		diff = this.minor - other.minor;
		if (diff != 0) {
			// Minor versions are different
			return diff;
		}
		// Major and minor versions are the same; everything depends on patch
		return this.patch - other.patch;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + major;
		result = prime * result + minor;
		result = prime * result + patch;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		if (patch != other.patch)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("V%d.%d.%d", major, minor, patch);
	}	
}
