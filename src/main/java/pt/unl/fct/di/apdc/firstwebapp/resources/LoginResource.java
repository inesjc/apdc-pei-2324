package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=uft-8")
public class LoginResource {
	
	/**
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	public LoginResource() {}	// Nothing to be done here
	
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Attemp to login user: " + data.username);
		return Response.ok().build();
	}
	
	
	
}
