package com.jenetics.smocker.model;

import javax.persistence.Entity;
import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jenetics.smocker.model.Communication;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Connection implements EntityWithId {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column(nullable = false)
	private String host;

	@Column(nullable = false)
	private Integer port;

	@OneToMany
	private Set<Communication> Communications = new HashSet<Communication>();

	@JoinColumn(nullable = false)
	@OneToOne @JsonIgnore 
	private JavaApplication javaApplication;

	@Column
	private Boolean watched;

	public JavaApplication getJavaApplication() {
		return javaApplication;
	}

	public void setJavaApplication(JavaApplication javaApplication) {
		this.javaApplication = javaApplication;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Connection)) {
			return false;
		}
		Connection other = (Connection) obj;
		if (id != null) {
			if (!id.equals(other.id)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Set<Communication> getCommunications() {
		return this.Communications;
	}

	public void setCommunications(final Set<Communication> Communications) {
		this.Communications = Communications;
	}

	public Boolean getWatched() {
		return watched;
	}

	public void setWatched(Boolean watched) {
		this.watched = watched;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (id != null)
			result += "id: " + id;
		result += ", version: " + version;
		if (host != null && !host.trim().isEmpty())
			result += ", host: " + host;
		if (port != null)
			result += ", port: " + port;
		if (Communications != null)
			result += ", Communications: " + Communications;
		if (javaApplication != null)
			result += ", javaApplication: " + javaApplication;
		if (watched != null)
			result += ", watched: " + watched;
		return result;
	}
}