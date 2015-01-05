package htlauncher.utilities;

import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(name="component")
@XmlSeeAlso({Version.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class ComponentDescriptor {
	private String name;
	private URI localURI;
	private URI serverURI;
	private Version version;
	
	public String getComponentName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}
	
	public Version getVersion(){
		return version;
	}
	
	public void setVersion(Version version){
		this.version = version;
	}
	
	public URI getLocalURI(){
		return localURI;
	}
	
	public void setLocalURI(URI local){
		this.localURI = local;
	}
	
	public URI getServerURI(){
		return serverURI;
	}
	
	public void setServerURI(URI server){
		this.serverURI = server;
	}
	

}
