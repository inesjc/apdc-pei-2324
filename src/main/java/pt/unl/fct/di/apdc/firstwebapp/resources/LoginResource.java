package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.TimestampValue;
import com.google.gson.Gson;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.Utils;

import java.util.logging.Logger;
import java.net.http.HttpHeaders;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
	
	/*
	@POST
    @Path("/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLoginV1(LoginData data) {
        LOG.fine("Attemp to login user: " + data.username);

        String status = isDataValid(data);
        if (!status.equals(Utils.SUCCESS)) {
            return Response.status(Status.BAD_REQUEST).entity(status).build();
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity entity = datastore.get(userKey);
        if (entity != null) {
            status = arePasswordsEqual(entity.getString("password"), DigestUtils.sha512Hex(data.password));
            if (status.equals(Utils.SUCCESS)) {
                AuthToken at = new AuthToken(data.username);
                Entity tempEntity = Entity.newBuilder(entity).set("last_login", Timestamp.now()).build();
                datastore.update(tempEntity);
                return Response.ok(g.toJson(at)).build();
            }
            return Response.status(Status.BAD_REQUEST).entity(status).build();
        }
        return Response.status(Status.NOT_FOUND).entity(Utils.USERNAME_NOT_EXISTS).build();
    }
    */
	
	@POST
    @Path("/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLoginV1(LoginData data) {
        LOG.fine("Attemp to login user: " + data.username);

        String status = isDataValid(data);
        if (!status.equals(Utils.SUCCESS)) {
            return Response.status(Status.BAD_REQUEST).entity(status).build();
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity entity = datastore.get(userKey);
        if (entity != null) {
            status = arePasswordsEqual(entity.getString("password"), DigestUtils.sha512Hex(data.password));
            if (status.equals(Utils.SUCCESS)) {
                TimestampValue now = TimestampValue.of(Timestamp.now());
                List<TimestampValue> lastLogins = new ArrayList<TimestampValue>();
                Date yesterday = Date.from(Instant.now().minusSeconds(24 * 3600));
                try {
                    List<TimestampValue> tempList = entity.getList("last_logins");
                    lastLogins.addAll(tempList);
                } catch (DatastoreException e) {
                    LOG.fine("Welcome to your first log in");
                }
                lastLogins.add(now);
                lastLogins.removeIf(t -> t.get().toDate().before(yesterday));
                AuthToken at = new AuthToken(data.username);

                Entity tempEntity = Entity.newBuilder(entity).set("last_logins", lastLogins).set("last_login", now)
                        .build();
                datastore.update(tempEntity);
                return Response.ok(g.toJson(at)).build();
            }
            return Response.status(Status.BAD_REQUEST).entity(status).build();
        }
        return Response.status(Status.NOT_FOUND).entity(Utils.USERNAME_NOT_EXISTS).build();
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
	
	@POST
    @Path("/user")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listLast24HourLogins(LoginData data) {
        LOG.fine("Attemp to recall logins from user in the last 24 hours: " + data.username);

        String status = isDataValid(data);
        if (!status.equals(Utils.SUCCESS)) {
            return Response.status(Status.BAD_REQUEST).entity(status).build();
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity entity = datastore.get(userKey);
        if (entity != null) {
            status = arePasswordsEqual(entity.getString("password"), DigestUtils.sha512Hex(data.password));
            if (status.equals(Utils.SUCCESS)) {
                List<TimestampValue> lastLogins = new LinkedList<TimestampValue>();
                Date yesterday = Date.from(Instant.now().minusSeconds(24 * 3600));
                try {
                    List<TimestampValue> tempList = entity.getList("lastlogins");
                    lastLogins.addAll(tempList);
                } catch (DatastoreException e) {
                    return Response.status(Status.EXPECTATION_FAILED).entity(Utils.USER_DIDNT_LOG_IN).build();
                }
                lastLogins.removeIf(t -> t.get().toDate().before(yesterday));

                if (lastLogins.size() > 0) {
                    return Response.ok(g.toJson(lastLogins.toArray())).build();
                } else {
                    return Response.status(Status.EXPECTATION_FAILED).entity(Utils.USER_DIDNT_LOG_IN).build();
                }
            }
            return Response.status(Status.BAD_REQUEST).entity(status).build();
        }
        return Response.status(Status.NOT_FOUND).entity(Utils.USERNAME_NOT_EXISTS).build();
    }
	

	
	// PRIVATE METHODS ----------------------------------------------
	
	private String areParamsNull(LoginData data) {
		String status = Utils.SUCCESS;
		if(Utils.isFieldNull(data.username) || Utils.isFieldNull(data.password))
			status = Utils.FIELDS_NULL;
		return status;
	}

	private String areParamsEmpty(LoginData data) {
		String status = Utils.SUCCESS;
		if(Utils.isFieldEmpty(data.username) || Utils.isFieldEmpty(data.password)) {
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
	
	private String isDataValid(LoginData data) {
		
		String status = areParamsNull(data);
		if (!status.equals(Utils.SUCCESS)) {
			return status;
		}
		
		status = areParamsEmpty(data);
		if (!status.equals(Utils.SUCCESS)) {
			return status;
		}
		
		return Utils.SUCCESS;
	}
	
	
}
