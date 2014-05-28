package ru.futurelink.mo.orm.security;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.PersistentManager;
import ru.futurelink.mo.orm.PersistentManagerSession;

@Entity(name = "Client")
@Table(name = "CLIENTS")
@NamedQueries({
	@NamedQuery(name="Client.keyExists", query="SELECT c FROM Client c where c.mOwner = :owner and c.mSecret = :secret")
})
public class Client extends CommonObject {
	private static final long serialVersionUID = 1L;

	protected Client() {}

	public Client(PersistentManagerSession manager) {
		super(manager);
	}

	@JoinColumn(name = "owner", referencedColumnName="id")
	private User mOwner;
	public void setOwner(User user) { mOwner = user; }
	public User getOwner() { return mOwner; }
	
	@Column(name = "secret")
	private String mSecret;
	public void setSecret(String secret) { mSecret = secret; }
	public String getSecret() { return mSecret; }  
}
