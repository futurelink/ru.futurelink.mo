/**
 * 
 */
package ru.futurelink.mo.orm;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Index;

import ru.futurelink.mo.orm.IPersistentManagerSession;
import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.exceptions.SaveException;

/**
 * @author pavlov
 *
 */
@Entity(name = "Register")
@Table(name = "REGISTER_COMMON")
@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="entityClass", discriminatorType=DiscriminatorType.STRING,length=250)
public class CommonRegister extends ModelObject {
	private static final long serialVersionUID = 1L;

	@Transient
	private IPersistentManagerSession pmSession;
	
	protected CommonRegister() {}
	
	public CommonRegister(IPersistentManagerSession pmSession) {
		this.pmSession = pmSession;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable=false)
	private Long mId;	
	@Override public String getId() { return mId.toString(); }
	@Override public void setId(String id) {}

	@Override public Boolean getDeleteFlag() { return false; }
	@Override public void setDeleteFlag(Boolean deleteFlag) {}

	@Index
	@Temporal((TemporalType.TIMESTAMP))
	@Column(name = "registeredDate")
	private Date date;
	public void setDate(Date date) { this.date = date; }
	public Date getDate() { return date; }

	@Override
	public Object save() throws SaveException {
		setDate(Calendar.getInstance().getTime());

		pmSession.getEm().persist(this);
		return this;
	}

	@Override
	public void saveCommit() throws SaveException {
		pmSession.transactionCommit();
	}
}
