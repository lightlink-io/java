<%

  request.getSession().setAttribute("test","testValue");
  var res = session.getAttribute("test");

  response.writeObject("res",res);

%>