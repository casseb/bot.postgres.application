package mvc;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import files.Box;
import objects.BoxFileObject;
import objects.BoxFolderObject;
import objects.HibernateUtil;
import objects.MessageLog;
import objects.Person;
import objects.PersonType;
import objects.Project;
import objects.ProjectStatus;
import objects.ProjectType;
import objects.Route;
import objects.RouteGroup;
import objects.ScheduleMessage;
import objects.Utils;


public class Model{
	
		//Variáveis Globais
	
		public List<Person> persons = new LinkedList<>();
		public List<Route> routes = new LinkedList<>();
		public List<ScheduleMessage> scheduleMessages = new LinkedList<>();
		
		public ProjectType projectType = ProjectType.MONETIZADO;
		public ProjectStatus projectStatus = ProjectStatus.IDEIA;
		public RouteGroup routeGroup = RouteGroup.CLIENTES;
		public Box box = null;
		public final ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);
		
		
		
		//Construtor
		
		@SuppressWarnings("unchecked")
		public Model(){
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Criteria critPerson = session.createCriteria(Person.class);
			critPerson.add(Restrictions.eq("personType",PersonType.PARCEIRO));
			critPerson.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			this.persons = (List<Person>) critPerson.list();
			Criteria critRoute = session.createCriteria(Route.class);
			this.routes = (List<Route>) critRoute.list();
			Criteria critScheduleMessage = session.createCriteria(ScheduleMessage.class);
			critScheduleMessage.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			this.scheduleMessages = (List<ScheduleMessage>) critScheduleMessage.list();
			session.close();
			
			List<String> routesString = new LinkedList<>();
			List<RouteGroup> routesGroup = new LinkedList<>();
			
			routesString.add("Termos");
			routesGroup.add(RouteGroup.INFORMACOES);
			
			routesString.add("Editar Login");
			routesGroup.add(RouteGroup.MEUSDADOS);
			
			routesString.add("Dar Permissão");
			routesGroup.add(RouteGroup.ADMINISTRATIVO);
			
			routesString.add("Remover Permissão");
			routesGroup.add(RouteGroup.ADMINISTRATIVO);
			
			routesString.add("Comandos");
			routesGroup.add(RouteGroup.NAVEGACAO);
			
			routesString.add("Dados dos Usuários");
			routesGroup.add(RouteGroup.ADMINISTRATIVO);
			
			routesString.add("Dados Login");
			routesGroup.add(RouteGroup.MEUSDADOS);
			
			routesString.add("Adicionar");
			routesGroup.add(RouteGroup.PROJETO);
			
			routesString.add("Remover Usuário");
			routesGroup.add(RouteGroup.ADMINISTRATIVO);
			
			routesString.add("Excluir");
			routesGroup.add(RouteGroup.PROJETO);
			
			routesString.add("Detalhes");
			routesGroup.add(RouteGroup.PROJETO);
			
			routesString.add("Editar");
			routesGroup.add(RouteGroup.PROJETO);
			
			routesString.add("Adicionar");
			routesGroup.add(RouteGroup.CLIENTES);
			
			routesString.add("Testes");
			routesGroup.add(RouteGroup.NAVEGACAO);
			
			routesString.add("Ativar Usuário");
			routesGroup.add(RouteGroup.ADMINISTRATIVO);
			
			routesString.add("Dar Permissão por menu");
			routesGroup.add(RouteGroup.ADMINISTRATIVO);
			
			routesString.add("Remover Permissão por menu");
			routesGroup.add(RouteGroup.ADMINISTRATIVO);
			
			routesString.add("Desativar Usuário");
			routesGroup.add(RouteGroup.ADMINISTRATIVO);
			
			for (int i = 0; i < routesString.size(); i++) {
				if(locateRoute(routesGroup.get(i).getDesc()+" - "+routesString.get(i))==null){
					addRoute(new Route(routesString.get(i),routesGroup.get(i)));
				}
			}
			
			
		}
		
		//Person-------------------------------------------------------------------
		
		public void addPerson(Person person){
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.save(person);
			session.getTransaction().commit();
			session.close();
			if(person.getPersonType().equals(PersonType.PARCEIRO)){
				persons.add(person);
				grandBasic(person);
			}
		}
		
		public void editPerson(Person person){
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.update(person);
			session.getTransaction().commit();
			session.close();
		}
		
		public Person locatePerson(String person){
			for (Person currentPerson : persons) {
				if(currentPerson.getName()!=null){
					if(currentPerson.getName().equals(person))
						return currentPerson;
				}
			}
			return null;
		}
		
		private boolean validatePassword(Person person,String senha){
			if (person.getSenha().equals(senha))
				return true;
			return false;
		}
		
		public Person locateTelegramUser(String idTelegram){
			for (Person person : persons) {
				if(person.getIdTelegram().equals(idTelegram))
					return person;
			}
			return null;
		}
			
		public Person addPersonByTelegram(String idTelegram){
			Person person = new Person(idTelegram);
			person.setPersonType(PersonType.PARCEIRO);
			addPerson(person);
			person.setIdTelegram(idTelegram);
			return person;
		}
		
		public void editUsernamePassword(Person person){
			editPerson(person);
		}
		
		public boolean havePermission(Route route,Person person){
			for (Route rota : person.getRotasPermitidas()) {
				if(rota.getCompleteWay().equals(route.getCompleteWay())){
					return true;
				}
			}
			return false;
		}
		
		public List<Route> routesDenieds(Person person){
			List<Route> result = new LinkedList<Route>();
			for (Route route : routes){
				if(!havePermission(route, person)){
					result.add(route);
				}
			}
			return result;
		}
		
		public void removePerson(Person person){
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.delete(person);
			session.getTransaction().commit();
			session.close();
			persons.remove(person);
		}
		
		//Route-------------------------------------------------------------------
		
		public void addRoute(Route route){
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.save(route);
			session.getTransaction().commit();
			session.close();
			routes.add(route);
		}
		
		public Route locateRoute(String route){
			for (Route currentRoute : routes) {
				if(currentRoute.getCompleteWay().equals(route))
					return currentRoute;
			}
			return null;
		}
		
		
		
		//Grand/Revoke Permissions-------------------------------------------------------------------
		
		
		public void grandBasic(Person person){
			grandPermission(person, RouteGroup.MEUSDADOS);
			grandPermission(person, RouteGroup.NAVEGACAO);
		}
		
		public Person revokePermission(Person person,Route route){
			List<Route> rotas = person.getRotasPermitidas();
			for (Route currentRoute : rotas) {
				if(currentRoute.getName().equals(route.getName())){
					rotas.remove(currentRoute);
					break;
				}
			}
			person.setRotasPermitidas(rotas);
			editPerson(person);
			return person;
		}
		
		public Person grandPermission(Person person, Route route){
			List<Route> rotas = person.getRotasPermitidas();
			rotas.add(route);
			person.setRotasPermitidas(rotas);
			editPerson(person);
			return person;
		}
		
		public void grandPermission(Person person, RouteGroup routeGroup){
			List<Route> routesToAdd = new LinkedList<>();
			for (Route route : routes) {
				if(route.getRouteGroup()==routeGroup){
					routesToAdd.add(route);
				}
				
			}
			
			for (Route route : routesToAdd) {
				grandPermission(person, route);
			}
		}
		
		public void revokePermission(Person person, RouteGroup routeGroup){
			List<Route> routesToRemove = new LinkedList<>();
			for (Route route : routes) {
				if(route.getRouteGroup()==routeGroup){
					routesToRemove.add(route);
				}
				
			}
			
			for (Route route : routesToRemove) {
				revokePermission(person, route);
			}
		}
		
		//Projets--------------------------------------------------------------------
		
		public void addProject(Project project){
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.save(project);
			session.getTransaction().commit();
			session.close();
		}
		
		public void removeProject(Project project){
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.delete(project);
			session.getTransaction().commit();
			session.close();
		}
		
		public Project addProjectByTelegram(String title, String description, ProjectType projectType, Person person){
			Project project = new Project(title,description,projectType,person);
			addProject(project);
			return project;
		}
		
		public List<Project> locateAllProjects(){
			List<Project> projects = new LinkedList<>();
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Criteria critProject = session.createCriteria(Project.class);
			critProject.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			projects = (List<Project>) critProject.list();
			session.close();
			return projects;
		}
		
		public Project locateProjectById(int id){
			List<Project> projects = locateAllProjects();
			for (Project project : projects) {
				if(project.getId()==id){
					return project;
				}
			}
			return null;
		}
		
		public Project locateProjectByString(String string){
			List<Project> projects = locateAllProjects();
			for (Project project : projects) {
				if(project.getId()==Integer.parseInt(string.substring(0,string.indexOf(" - ")))){
					return project;
				}
			}
			return null;
		}
		
		public void editProject(Project project){
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.update(project);
			session.getTransaction().commit();
			session.close();
		}
		
		//Cliente--------------------------------------------------------------------
		
		public void addClient(Person person){
			person.setPersonType(PersonType.CLIENTE);
			addPerson(person);
		}
		
		//Visualizações Bot----------------------------------------------------------
		
		public List<String> showGroupRoutes(List<Route> rotas){
			if(rotas == null){
				return null;
			}
			List<String> saida = new LinkedList<>();
			
			for (RouteGroup routeGroup : RouteGroup.values()) {
				boolean first = true;
				for (Route route : rotas) {
					if(route.getRouteGroup().equals(routeGroup)){
						if(first){
							saida.add(routeGroup.desc);
							first = false;
						}
					}
				}
			}
			
			return saida;
		}
		
		public List<String> showRoutes(RouteGroup routeGroup, List<Route> rotas){
			if(routeGroup == null){
				return null;
			}
			List<String> saida = new LinkedList<>();
			
			for (Route route : rotas) {
				if(route.getRouteGroup().equals(routeGroup)){
					saida.add(route.getName()+"\n");
					}
					
				}
			
			return saida;
		}
		
		public String showRoutes(List<Route> rotas){
			if(rotas == null){
				return "Nenhuma rota";
			}
			StringBuilder saida = new StringBuilder();
			
			for (RouteGroup routeGroup : RouteGroup.values()) {
				boolean first = true;
				for (Route route : rotas) {
					if(route.getRouteGroup().equals(routeGroup)){
						if(first){
							saida.append(routeGroup.desc + "------------\n");
							first = false;
						}
						saida.append(route.getName()+"\n");
					}
				}
			}
			
			return saida.toString();
		}
		
		public List<String> showPersons(List<Person> persons){
			if(persons == null){
				return null;
			}
			List<String> saida = new LinkedList<>();
			for (Person person : persons) {
				saida.add(person.getName());
			}
			return saida;
		}
		
		public String showUserDataWithoutPassword(Person person){
			if(persons == null){
				return "Nenhuma pessoa";
			}
			StringBuilder saida = new StringBuilder();
			saida.append("\n Id do Telegram: " + person.getIdTelegram());
			saida.append("\n Nome do Usuário: "+ person.getName());
			return saida.toString();
		}
		
		public String showUserData(Person person){
			if(persons == null){
				return "Nenhuma pessoa";
			}
			StringBuilder saida = new StringBuilder();
			saida.append("\n Id do Telegram: " + person.getIdTelegram());
			saida.append("\n Nome do Usuário: "+ person.getName());
			saida.append("\n Senha definida: "+ person.getSenha());
			return saida.toString();
		}
		
		public List<String> showProjects(List<Project> projects){
			if(projects == null){
				return null;
			}
			List<String> saida = new LinkedList<String>();
			for (Project project : projects) {
				saida.add(project.toString());
			}
			return saida;
			
		}
		
		public String showProject(Project project){
			StringBuilder saida = new StringBuilder();
			saida.append("\n Id do Projeto: "+project.getId());
			saida.append("\n Título: "+project.getTitle());
			saida.append("\n Descrição: "+project.getDesc());
			saida.append("\n Tipo: "+project.getType().description);
			saida.append("\n Status: "+project.getStatus().description+"\n");
			return saida.toString();
			
		}
		
		public List<String> showProjectTypes(){
			ProjectType[] types = ProjectType.values();
			List<String> saida = new LinkedList<>();
			for (ProjectType projectType : types) {
				saida.add(projectType.description);
			}
			return saida;
		}
		
		public List<String> showProjectStatus(){
			ProjectStatus[] status = ProjectStatus.values();
			List<String> saida = new LinkedList<String>();
			for (ProjectStatus projectStatus : status) {
				saida.add(projectStatus.description);
			}
			return saida;
		}
		
		//BoxFileObject------------------------------------------------------------------
		
		public void inicializeBox() {
			if(box == null)
			box = new Box();
		}
		
		public void addBoxFileObject(BoxFileObject boxFileObject){
			inicializeBox();
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.save(boxFileObject);
			session.getTransaction().commit();
			session.close();
		}
		
		public BoxFileObject locateBoxFileObjectByName(String name, String... folder){
			inicializeBox();
			List<BoxFileObject> boxFileObjects = new LinkedList<>();
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Criteria critBoxFileObject = session.createCriteria(BoxFileObject.class);
			boxFileObjects = (List<BoxFileObject>) critBoxFileObject.list();
			session.close();
			
			BoxFolderObject currentFolder = null;
			BoxFolderObject rootFolder = null;
			
			for (String string : folder) {
				currentFolder = locateBoxFolderObjectByName(string);
				if(currentFolder == null){
					return null;
				}
				rootFolder = currentFolder;
			}
			
			for (BoxFileObject boxFileObject : boxFileObjects) {
				inicializeBox();
				if(boxFileObject.getName().equals(name) && boxFileObject.getBoxFolderObject().getId() == currentFolder.getId()){
					return boxFileObject;
				}
			}
			
			return null;
		}
		
		//BoxFolderObject------------------------------------------------------------------
		
				public void addBoxFolderObject(BoxFolderObject boxFolderObject){
					inicializeBox();
					Session session = HibernateUtil.getSessionFactory().openSession();
					session.beginTransaction();
					session.save(boxFolderObject);
					session.getTransaction().commit();
					session.close();
				}
				
				public BoxFolderObject locateBoxFolderObjectByName(String name){
					inicializeBox();
					List<BoxFolderObject> boxFolderObjects = new LinkedList<>();
					Session session = HibernateUtil.getSessionFactory().openSession();
					session.beginTransaction();
					Criteria critBoxFolderObject = session.createCriteria(BoxFolderObject.class);
					boxFolderObjects = (List<BoxFolderObject>) critBoxFolderObject.list();
					session.close();
					
					for (BoxFolderObject boxFolderObject : boxFolderObjects) {
						if(boxFolderObject.getName().equals(name)){
							return boxFolderObject;
						}
					}
					
					return null;
				}
				
		//MessageLog---------------------------------------------------------------------
				
				public void addMessageLog(MessageLog messageLog){
					/*
					Session session = HibernateUtil.getSessionFactory().openSession();
					session.beginTransaction();
					session.save(messageLog);
					session.getTransaction().commit();
					session.close();
					*/
				}
				
				
		
		//Schedule------------------------------------------------------------------------
				
				public void addScheduleMessage(ScheduleMessage scheduleMessage){
					Session session = HibernateUtil.getSessionFactory().openSession();
					session.beginTransaction();
					session.save(scheduleMessage);
					session.getTransaction().commit();
					session.close();
				}
				
				public void removeScheduleMessage(ScheduleMessage scheduleMessage){
					Session session = HibernateUtil.getSessionFactory().openSession();
					session.beginTransaction();
					session.delete(scheduleMessage);
					session.getTransaction().commit();
					session.close();
				}
				
		
		//Métodos REST-------------------------------------------------------------------
		
		public JSONObject login(JSONObject json){        	
	        String usuario = json.getString("usuario");
	        String senha = json.getString("senha");				
			try {									
				 if(validatePassword(locatePerson(usuario), senha)){
					 JSONArray jsonResult = new JSONArray();
		         	 JSONObject jsonObj = new JSONObject();
		         	 jsonObj.put("userName", usuario);
		             jsonResult.put(jsonObj);			             	
		             return jsonObj;
					}else{
					}				
			} catch (JSONException e) {
				//e.printStackTrace();
			}
			JSONArray jsonResult = new JSONArray();
    	    JSONObject jsonObj = new JSONObject();
    	    jsonObj.put("status", "");
    	    jsonResult.put(jsonObj);
        	return jsonObj;
		}
		
		public JSONArray termos() throws IOException{
			try {
				
				JSONArray json = Utils.readJsonFromUrl("https://api.myjson.com/bins/x7jzr");
				return json;
				
			} catch (JSONException e) {

				e.printStackTrace();

			}
			return null;
		}


		
		
}
