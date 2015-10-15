<%

  env.getRequest().getSession().setAttribute("test","testValue");
  var res = env.getSession().getAttribute("test");

  response.writeObject("res",res);

%>