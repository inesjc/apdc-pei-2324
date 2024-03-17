package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Entity;
import com.google.gson.Gson;
import com.google.cloud.Timestamp;

import pt.unl.fct.di.apdc.firstwebapp.util.*;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

	/**
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public RegisterResource() {
	} // Nothing to be done here

	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegister(LoginData data) {
		LOG.fine("Attemp to register user: " + data.username);
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity person = Entity.newBuilder(userKey).set("password", DigestUtils.sha512Hex(data.password))
				.set("time_of_registration", Timestamp.now()).build();
		try {
			datastore.add(person);
		} catch (DatastoreException e) {
			if ("ALREADY_EXISTS".equals(e.getReason())) {
				// entity.getKey() already exists
				return Response.status(Status.FORBIDDEN).entity(Utils.USERNAME_IN_USE).build();
			}
		}
		AuthToken at = new AuthToken(data.username);
		return Response.ok(g.toJson(at)).build();
	}

	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegisterV2(LoginDataV2 data) {
		LOG.fine("Attemp to register user: " + data.username);
				
		String status = isDataValid(data);
		if (!status.equals(Utils.SUCCESS)) {
			return Response.status(Status.BAD_REQUEST).entity(status).build();
		}
		
		//TODO: perguntar ao prof como e com as transacoes no nosso caso
		// porque nao faz sentido fazer um get e um add visto q add ja faz get e porque
		// sao 2 pesquisas O(n), aqui poderemos nao precisar de fazer transacao
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity person = Entity.newBuilder(userKey)
						.set("password", DigestUtils.sha512Hex(data.password))
						.set("time_of_registration", Timestamp.now())
						.set("email", data.email)
						.set("name", data.name)
						.build();

		try {
			datastore.add(person);
		} catch (DatastoreException e) {
			if ("ALREADY_EXISTS".equals(e.getReason())) {
				return Response.status(Status.FORBIDDEN).entity(Utils.USERNAME_IN_USE).build();
			}
		}
		AuthToken at = new AuthToken(data.username);
		return Response.ok(g.toJson(at)).build();
	}
	
	// PRIVATE METHODS --------------------------------------------------------------------------------

	private String areParamsNull(LoginDataV2 data) {
		String status = Utils.SUCCESS;
		if(Utils.isFieldNull(data.username) || Utils.isFieldNull(data.password)
				|| Utils.isFieldNull(data.confirmation) || Utils.isFieldNull(data.email)
				|| Utils.isFieldNull(data.name))
			status = Utils.FIELDS_NULL;
		return status;
	}

	private String areParamsEmpty(LoginDataV2 data) {
		String status = Utils.SUCCESS;
		if(Utils.isFieldEmpty(data.username) || Utils.isFieldEmpty(data.password)
				|| Utils.isFieldEmpty(data.confirmation) || Utils.isFieldEmpty(data.email)
				|| Utils.isFieldEmpty(data.name)) {
			status = Utils.FIELDS_EMPTY;
			
		}
		return status;

	}

	private String arePasswordsEqual(String password1, String confirmation) {
		String status = Utils.SUCCESS;
		if(!Utils.areFieldsEqual(password1, confirmation)) {
			status = Utils.PW_NO_MATCH;
		}
		return status;
	}
	
	private String isDataValid(LoginDataV2 data) {
		
		String status = areParamsNull(data);
		if (!status.equals(Utils.SUCCESS)) {
			return status;
		}
		
		status = areParamsEmpty(data);
		if (!status.equals(Utils.SUCCESS)) {
			return status;
		}
		
		status = Utils.isEmailValid(data.email);
		if (!status.equals(Utils.SUCCESS)) {
			return status;
		}
		status = Utils.isNameValid(data.name);
		if (!status.equals(Utils.SUCCESS)) {
			return status;
		}
		status = Utils.isUsernameValid(data.username);
		if (!status.equals(Utils.SUCCESS)) {
			return status;
		}
		
		status = Utils.isPasswordValid(data.password);
		if (!status.equals(Utils.SUCCESS)) {
			return status;
		}
		
		status = arePasswordsEqual(data.password, data.confirmation);
		if (!status.equals(Utils.SUCCESS)) {
			return status;
		}
		return Utils.SUCCESS;
	}

}
