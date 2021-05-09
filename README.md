# LibraryManager2
Books Library Management system using REST API and java server

modules:-

1.login/signup reader

2.home page with left navigation panel

3.viewing books in library..

4.applying filters on books search

5.select some books upto readers limit and reserve them

6.my dashboard- viewing (hand in books, reserved books, returned books) - action(reserved books can be cancelled by selecting set of books checkbox)

7.check in module -viewing (hand in books, reserved books)- actions(reserved->handin || handin->return) by selecting set of books checkbox

8.logout- clear localStorage login creds


every actions involve user id/psw validation at java methods with GET, POST, PUT, DELETE <REST API> methods

for every action JSON request body is built &sent to java server where it is MIME text->user defined object (XMLRootelement) and handled​
