package ru.futurelink.mo.orm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.JoinColumn;

/**
 * ��������� ����� ���������.
 * 
 * ������ ������ �������� ��� �������� � ���� ����������������� ID,
 * �������� ������ �������� � ������� ID ����� �������� � ����� �������.
 * 
 * @author Futurelink
 * @since 0.1
 *
 */
@Entity(name = "CodeSupport")
@Table(name = "CODES")
public class CodeSupport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long mId;
	public Long getId() { return mId; }

	@JoinColumn(name = "object", referencedColumnName="id")
	private CommonObject mObject;
	public  CommonObject getObject() { return mObject; }
	public void setObject(CommonObject object) { mObject = object; }

	@Column(name = "objectClass")
	private String mObjectClass;
	public void setObjectClass(String className) { mObjectClass = className; }
	public String getObjectClass() { return mObjectClass; }

}
