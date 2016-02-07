$(function() {

    var ul = $('#users-list');
    var discussion = $('#discussion');

    var addToDiscussion = function(txt) {
        discussion.append('<li>' + txt + '</li>');
    };

    var users = [];

    var refreshUsers = function() {
        ul.empty();
        users.forEach(function(u) {
            ul.append('<li>' + u + '</li>');
        });
    };

    var messageHandlers = {
        'UsersList': function(msg) {
            users = msg.users;
            refreshUsers();
        },
        'UserEntered': function(msg) {
            addToDiscussion(msg.username + ' entered the chat');
            users.push(msg.username);
            refreshUsers();
        },
        'UserLeft': function(msg) {
            addToDiscussion(msg.username + ' left the chat');
            users.splice(users.indexOf(msg.username), 1);
            refreshUsers();
        },
        'UserDisconnected': function(msg) {
            debugger;
            addToDiscussion(msg.username + ' left the chat (disconnected)');
            users.splice(users.indexOf(msg.username), 1);
            refreshUsers();
        },
        'MessagePosted': function(msg) {
            addToDiscussion(msg.username + ':' + msg.message);
        }
    };

    // connect to messages
    // we use old school XHR API because it's messy to
    // do this with JQuery...
    var xhr = new XMLHttpRequest();
    var offset = 0;
    xhr.onreadystatechange = function() {
        if ((xhr.readyState == 3 || xhr.readyState == 4) && xhr.status == 200) {
            // get last received text from response
            var responseText = xhr.responseText;
            var lastResp = responseText.substring(offset, responseText.length);
            offset += lastResp.length;
            // "parse" last response text
            var start = '<div class="'.length;
            var className = '';
            while (start < lastResp.length) {
                var c = lastResp.charAt(start++);
                if (c == '"') {
                    break;
                }
                className += c;
            }
            var handler = messageHandlers[className];
            if (handler) {
                // deserialize message contents
                // and call handler function
                var rest = lastResp.substring(start + '">'.length - 1, lastResp.length);
                rest = rest.substring(0, rest.lastIndexOf('</div>'));
                var msg = JSON.parse(rest);
                handler.apply(this, [msg])
            }
        }
    };
    xhr.open("GET", chat.context + "/api/messages", true);
    xhr.send();

    var sendBtn = $('#send');
    var textElem = $('#text');

    var sendMsg = function() {
        sendBtn.prop('disabled', true);
        textElem.prop('disabled', true);
        $.post(chat.context + '/api/post', {
            message: textElem.val()
        }).done(function(resp) {
            textElem.val('');
            textElem.prop('disabled', false);
            sendBtn.prop('disabled', false);
        });
    };

    sendBtn.click(sendMsg);
    textElem.keypress(function(e) {
        if(e.which == 13) {
            sendMsg();
        }
    });


});