package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.Utils;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	
	/**
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Gson g = new Gson();
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public LoginResource() {}	// Nothing to be done here
	
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Attemp to login user: " + data.username);
		if(data.username.equals("jleitao") && data.password.equals("password")) {
			AuthToken at = new AuthToken(data.username);
			return Response.ok(g.toJson(at)).build();
		}
		return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
	}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLoginV1(LoginData data) {
		LOG.fine("Attemp to login user: " + data.username);
		
		if(data.username == null || data.password == null) {
			return Response.status(Status.BAD_REQUEST).entity("At least one field is null.").build();
		}
		if(data.username.isEmpty() || data.password.isEmpty()) {
			return Response.status(Status.BAD_REQUEST).entity("At least one field is empty.").build();
		}
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity e = datastore.get(userKey);
		if(e != null) {
			if(Utils.areFieldsEqual(e.getString("password"), data.password)) {
				AuthToken at = new AuthToken(data.username);
				return Response.ok(g.toJson(at)).build();
			}
			return Response.status(Status.NOT_FOUND).entity("Wrong password.").build();
		}
		return Response.status(Status.NOT_FOUND).entity("Username does not exist, please register.").build();
	}
	
	
	@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		if(username.trim().equals("jleitao")) {
			return Response.ok().entity(g.toJson(false)).build();
		}
		else {
			return Response.ok().entity(g.toJson(true)).build();
		}
	}
	
	
}
