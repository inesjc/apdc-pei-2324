package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Cursor;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginDataV2;
import pt.unl.fct.di.apdc.firstwebapp.util.Utils;

import java.util.logging.Logger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LoginResource() {

	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);
		if (data.username.equals("jleitao") && data.password.equals("password")) {
			AuthToken at = new AuthToken(data.username);
			return Response.ok(g.toJson(at)).build();
		}
		return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
	}
	
	/*
	 * First set of slides: Exercise 3
	@POST
    @Path("/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLoginV1(LoginData data) {
        LOG.fine("Attemp to login user: " + data.username);

        String status = isDataValid(data);
        if (!status.equals(Utils.SUCCESS)) {
        	LOG.warning(status);
            return Response.status(Status.BAD_REQUEST).build();
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = datastore.get(userKey);
        if (user != null) {
            status = arePasswordsEqual(user.getString("password"), DigestUtils.sha512Hex(data.password));
            if (status.equals(Utils.SUCCESS)) {
                AuthToken at = new AuthToken(data.username);
                return Response.ok(g.toJson(at)).build();
            }
            LOG.warning(status);
            return Response.status(Status.BAD_REQUEST).build();
        }
        LOG.warning(status);
        return Response.status(Status.NOT_FOUND).build();
    }
    */
	
	/*
	 * First set of slides: Tarefa 5 
	 * Login with timestamp of lastLogin
	@POST
    @Path("/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLoginV1(LoginData data) {
        LOG.fine("Attemp to login user: " + data.username);

        String status = isDataValid(data);
        if (!status.equals(Utils.SUCCESS)) {
        	LOG.warning(status);
            return Response.status(Status.BAD_REQUEST).build();
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = datastore.get(userKey);
        if (user != null) {
            status = arePasswordsEqual(user.getString("password"), DigestUtils.sha512Hex(data.password));
            if (status.equals(Utils.SUCCESS)) {
                AuthToken at = new AuthToken(data.username);
                Entity lastLogin = Entity.newBuilder(user).set("last_login", Timestamp.now()).build();
                datastore.update(lastLogin);
                return Response.ok(g.toJson(at)).build();
            }
            LOG.warning(status);
            return Response.status(Status.BAD_REQUEST).build();
        }
        LOG.warning(status);
        return Response.status(Status.NOT_FOUND).build();
    }
    */

	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLoginV1(LoginData data) {
		LOG.fine("Attemp to login user: " + data.username);

		String status = isDataValid(data);
		if (!status.equals(Utils.SUCCESS)) {
			LOG.warning(status);
			return Response.status(Status.BAD_REQUEST).build();
		}

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);
		Key logKey = datastore.allocateId(datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
				.setKind("UserLog").newKey());
		if (user != null) {
			status = arePasswordsEqual(user.getString("password"), DigestUtils.sha512Hex(data.password));
			if (status.equals(Utils.SUCCESS)) {
				Timestamp now = Timestamp.now();
				Entity log = Entity.newBuilder(logKey).set("user_login_time", now).build();

				AuthToken at = new AuthToken(data.username);

				datastore.put(log);

				return Response.ok(g.toJson(at)).build();
			} else {
				LOG.warning("Wrong password for: " + data.username);
				LOG.warning(status);
				return Response.status(Status.FORBIDDEN).build();
			}
		} else {
			LOG.warning("Failed login attempt for username: " + data.username);
			LOG.warning(status);
			return Response.status(Status.FORBIDDEN).build();
		}
	}

	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLoginV2(LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {
		LOG.fine("Attemp to login user: " + data.username);
		Timestamp now = Timestamp.now();
		String status = isDataValid(data);
		if (!status.equals(Utils.SUCCESS)) {
			LOG.warning(status);
			return Response.status(Status.BAD_REQUEST).build();
		}

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Key countersKeys = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
				.setKind("userStats").newKey("counters");
		Key logKey = datastore.allocateId(datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
				.setKind("UserLog").newKey());

		Entity entity = datastore.get(userKey);
		Entity stats = datastore.get(countersKeys);

		if (entity != null) {
			if (stats == null) {
				stats = Entity.newBuilder(countersKeys).set("user_stats_logins", 0L).set("user_stats_failed", 0L)
						.set("user_last_login", now).set("user_first_login", now).build();
			}
			status = arePasswordsEqual(entity.getString("password"), DigestUtils.sha512Hex(data.password));
			if (status.equals(Utils.SUCCESS)) {

				Entity log = Entity.newBuilder(logKey).set("user_login_ip", request.getRemoteAddr())
						.set("user_login_host", request.getRemoteHost())
						.set("user_login_latlon",
								StringValue.newBuilder(headers.getHeaderString("X-AppEngine-CityLatLong"))
										.setExcludeFromIndexes(true).build())
						.set("user_login_city", headers.getHeaderString("X-AppEngine-City"))
						.set("user_login_country", headers.getHeaderString("X-AppEngine-Country"))
						.set("user_login_time", now).build();

				Entity ustats = Entity.newBuilder(countersKeys)
						.set("user_stats_logins", 1L + stats.getLong("user_stats_logins")).set("user_stats_failed", 0L)
						.set("user_last_login", now).set("user_first_login", stats.getTimestamp("user_first_login"))
						.build();
				datastore.put(ustats, log);

				AuthToken at = new AuthToken(data.username);

				return Response.ok(g.toJson(at)).build();
			}
			Entity ustats = Entity.newBuilder(countersKeys).set("user_stats_logins", stats.getLong("user_stats_logins"))
					.set("user_stats_failed", 1L + stats.getLong("user_stats_failed"))
					.set("user_last_login", stats.getTimestamp("user_last_login"))
					.set("user_first_login", stats.getTimestamp("user_first_login")).set("user_last_attempt", now)
					.build();
			datastore.put(ustats);
			LOG.warning(status);
			return Response.status(Status.BAD_REQUEST).build();
		}
		LOG.warning(status);
		return Response.status(Status.NOT_FOUND).build();
	}

	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listLast24HourLogins(LoginData data) {
		LOG.fine("Attemp to recall logins from user in the last 24 hours: " + data.username);

		String status = isDataValid(data);
		if (!status.equals(Utils.SUCCESS)) {
			LOG.warning(status);
			return Response.status(Status.BAD_REQUEST).build();
		}
		Timestamp yesterday = Timestamp.of(Date.from(Instant.now().minusSeconds(24 * 60 * 60)));
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("UserLog")
				.setFilter(CompositeFilter.and(
						PropertyFilter.hasAncestor(datastore.newKeyFactory().setKind("User").newKey(data.username)),
						PropertyFilter.ge("user_login_time", yesterday)))
				.build();

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity entity = datastore.get(userKey);
		if (entity != null) {
			status = arePasswordsEqual(entity.getString("password"), DigestUtils.sha512Hex(data.password));
			if (status.equals(Utils.SUCCESS)) {

				QueryResults<Entity> logs = datastore.run(query);
				List<Date> loginDates = new ArrayList<Date>();
				logs.forEachRemaining(userLog -> {
					loginDates.add(userLog.getTimestamp("user_login_time").toDate());
				});
				LOG.warning(status);
				return Response.ok(g.toJson(loginDates)).build();
			}
			LOG.warning(status);
			return Response.status(Status.BAD_REQUEST).build();
		}
		LOG.warning(status);
		return Response.status(Status.NOT_FOUND).build();
	}
	
	@POST
    @Path("/user/pagination")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listLast24HourLogins3x3(LoginData data, @Context HttpServletRequest req) {
        LOG.fine("Attemp to recall logins from user: " + data.username);

        String status = isDataValid(data);
        if (!status.equals(Utils.SUCCESS)) {
            return Response.status(Status.BAD_REQUEST).entity(status).build();
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity entity = datastore.get(userKey);
        if (entity != null) {
            status = arePasswordsEqual(entity.getString("password"), DigestUtils.sha512Hex(data.password));
            if (status.equals(Utils.SUCCESS)) {
                Timestamp yesterday = Timestamp.of(Date.from(Instant.now().minusSeconds(24 * 60 * 60)));
                Query<Entity> query;
                String startCursor = req.getParameter("cursor");
                if (startCursor != null) {
                    query = Query.newEntityQueryBuilder().setKind("UserLog").setLimit(3)
                            .setStartCursor(Cursor.fromUrlSafe(startCursor))
                            .setFilter(CompositeFilter.and(
                                    PropertyFilter.hasAncestor(
                                            datastore.newKeyFactory().setKind("User").newKey(data.username)),
                                    PropertyFilter.ge("user_login_time", yesterday)))
                            .build();
                } else {

                    query = Query.newEntityQueryBuilder().setKind("UserLog").setLimit(3)
                            .setFilter(CompositeFilter.and(
                                    PropertyFilter.hasAncestor(
                                            datastore.newKeyFactory().setKind("User").newKey(data.username)),
                                    PropertyFilter.ge("user_login_time", yesterday)))
                            .build();
                }

                QueryResults<Entity> logs = datastore.run(query);
                List<Date> loginDates = new ArrayList<Date>();
                logs.forEachRemaining(userLog -> {
                    loginDates.add(userLog.getTimestamp("user_login_time").toDate());
                });
                return Response.ok(g.toJson(loginDates) + logs.getCursorAfter().toUrlSafe()).build();
            } else {
                LOG.warning("Wrong password for: " + data.username);
                return Response.status(Status.FORBIDDEN).build();
            }
        } else {
            LOG.warning("Failed login attempt for username: " + data.username);
            return Response.status(Status.FORBIDDEN).build();
        }
    }

	@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
		Entity entity = datastore.get(userKey);
		if (entity != null) {
			return Response.ok().entity(g.toJson(false)).build();
		} else {
			return Response.ok().entity(g.toJson(true)).build();
		}
	}
	
	@POST
    @Path("/get")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUserSelf(LoginData data) {
        LOG.fine("Attemp to get information from user: " + data.username);

        String status = isDataValid(data);
        if (!status.equals(Utils.SUCCESS)) {
            return Response.status(Status.BAD_REQUEST).entity(status).build();
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity entity = datastore.get(userKey);
        if (entity != null) {
            status = arePasswordsEqual(entity.getString("password"), DigestUtils.sha512Hex(data.password));
            if (status.equals(Utils.SUCCESS)) {
                String[] intel = { "username : " + data.username, "email : " + entity.getString("email"),
                        "name : " + entity.getString("name") };
                return Response.ok().entity(g.toJson(intel)).build();
            } else {

                LOG.warning("Wrong password for: " + data.username);
                return Response.status(Status.FORBIDDEN).build();

            }
        } else {

            LOG.warning("Failed fetch attempt for username: " + data.username);
            return Response.status(Status.FORBIDDEN).build();

        }
    }

    @GET
    @Path("/get/{user}")
    public Response getUser(@PathParam("user") String user) {
        LOG.fine("Attemp to get information without password from user: " + user);

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(user);
        Entity entity = datastore.get(userKey);
        if (entity != null) {
            String[] intel = { "username : " + user, "email : " + entity.getString("email"),
                    "name : " + entity.getString("name") };
            return Response.ok().entity(g.toJson(intel)).build();

        } else {

            LOG.warning("Failed fetch attempt for username: " + user);
            return Response.status(Status.FORBIDDEN).build();

        }
    }

	@DELETE
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deLete(LoginDataV2 data) {
		LOG.fine("Attemp to delete user: " + data.name);
		if (data.username.equals("admin") && data.password.equals("admin") && data.confirmation.equals("password")
				&& data.email.equals("admin@admin.admin")) {

			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.name);
			Entity entity = datastore.get(userKey);
			datastore.delete(userKey);

			return Response.ok(g.toJson(entity)).build();
		}
		return Response.status(Status.FORBIDDEN).build();
	}
	
	@DELETE
	@Path("/delete/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deLeteV2(LoginData data) {
		LOG.fine("Attemp to delete user: " + data.username);
	
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity entity = datastore.get(userKey);
		
		if(entity != null) {
			if(entity.getString("password").equals(DigestUtils.sha512Hex(data.password))) {
				datastore.delete(userKey);
				return Response.ok(g.toJson(entity)).build();
				
			}
			return Response.status(Status.BAD_REQUEST).build();
		}
		return Response.status(Status.FORBIDDEN).build();
	}

	private String areParamsNull(LoginData data) {
		String status = Utils.SUCCESS;
		if (Utils.isFieldNull(data.username) || Utils.isFieldNull(data.password))
			status = Utils.FIELDS_NULL;
		return status;
	}

	private String areParamsEmpty(LoginData data) {
		String status = Utils.SUCCESS;
		if (Utils.isFieldEmpty(data.username) || Utils.isFieldEmpty(data.password)) {
			status = Utils.FIELDS_EMPTY;

		}
		return status;

	}

	private String arePasswordsEqual(String password1, String confirmation) {
		String status = Utils.SUCCESS;
		if (!Utils.areFieldsEqual(password1, confirmation)) {
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
