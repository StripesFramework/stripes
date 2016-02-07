<%@ page import="net.sourceforge.stripes.chatdemo.actions.LoginActionBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="/WEB-INF/layout.jsp" title="Chat">
    <stripes:layout-component name="head">
        <script type="text/javascript">
            window.chat = {
                context: '${pageContext.request.contextPath}'
            };
        </script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.9.1.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/chat.js"></script>
    </stripes:layout-component>
    <stripes:layout-component name="body">
        <div class="main-container">
            <div class="header">
                <div class="brand">
                    <h1>Stripes Chat Demo</h1>
                </div>
                <div class="user-info">
                    Logged in as
                    <em>${actionBean.username}</em>
                    -
                    <stripes:link beanclass="<%=LoginActionBean.class%>" event="logout">
                        Logout
                    </stripes:link>
                </div>
            </div>
            <div class="body">
                <div class="users">
                    <h2>Users</h2>
                    <ul id="users-list">
                    </ul>
                </div>
                <div class="chat-win">
                    <div class="discussion">
                        <h2>Discussion</h2>
                        <ul id="discussion">
                        </ul>
                    </div>
                    <div class="footer">
                        <div class="message">
                            <input id="text" type="text" name="message" placeholder="Type your message and press ENTER">
                        </div>
                        <div class="button">
                            <button id="send">Send</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
