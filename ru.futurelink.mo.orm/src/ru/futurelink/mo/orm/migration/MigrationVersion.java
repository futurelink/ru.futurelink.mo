/**
 * 
 */
package ru.futurelink.mo.orm.migration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author pavlov
 *
 */
@Entity
@Table(name = "VERSION")
public class MigrationVersion {
	public MigrationVersion() {}

	@Id
	@GeneratedValue(generator="system-uuid")
	@Column(name = "id", columnDefinition="VARCHAR(64)", nullable=false)
	private String mId;
	public String getId() { return mId; }

	/**
	 * Номер версии базы данных.
	 */
	@Column(name = "version")
	private Integer mVersion;
	public Integer getVersion() { return mVersion; }	
	public void setVersion(Integer version) { mVersion = version; }
	
	/**
	 * Ревизия данной версии, из SVN или GIT.
	 */
	@Column(name = "revision")
	private String mRevision;
	public String getRevision() { return mRevision; }
	public void setRevision(String revision) { mRevision = revision; }
}
