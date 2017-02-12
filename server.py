import cgi
import json
import time
import BaseHTTPServer
import urlparse
from urlparse import parse_qs

HOST_NAME = '0.0.0.0'
PORT_NUMBER = 8000


class Books(object):
    def __init__(self, id, author, year, title, publishing, status, user_id=None):
        self.id = id
        self.author = author
        self.year = year
        self.title = title
        self.publishing = publishing
        self.status = status
        self.user_id = user_id


class Users(object):
    def __init__(self, id, username, password):
        self.id = id
        self.username = username
        self.password = password


class Handler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_HEAD(self):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()

    def do_GET(self):
        """Respond to a GET request."""
        parsed_path = urlparse.urlparse(self.path)
        if parsed_path.path == '/books':
            return self.books()
        elif parsed_path.path == '/my_loans':
            return self.my_loans()
        return self.send_response(404)

    def do_POST(self):
        parsed_path = urlparse.urlparse(self.path)
        form = cgi.FieldStorage(
            fp=self.rfile,
            headers=self.headers,
            environ={'REQUEST_METHOD': 'POST',
                     'CONTENT_TYPE': self.headers['Content-Type'],
                     })
        if parsed_path.path == '/login':
            return self.login(form)
        elif parsed_path.path == '/get_loan':
            return self.get_loan(form)
        elif parsed_path.path == '/return_book':
            return self.return_book(form)
        return self.send_response(404)

    def books(self):
        try:
            parsed_path = urlparse.urlparse(self.path)
            query = parse_qs(parsed_path.query, keep_blank_values=True)

            if 'status' in query:
                status_book = {}
                for book in books:
                    if books[book].status == str(query['status'][0]):
                        status_book[book] = books[book]
                if not status_book:
                    self.send_response(404)
                self.send_response(200)
                self.send_header("Content-type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps(status_book, cls=LibraryEncoder))
            else:
                self.send_response(200)
                self.send_header("Content-type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps(books, cls=LibraryEncoder))
        except Exception as e:
            self.send_response(404)
            print 'Exception in books: ', e

    def get_loan(self, form):
        try:
            if books[int(form['book_id'].value)].status == 'on_loan':
                self.send_response(404)
                return 404
            books[int(form['book_id'].value)].status = 'on_loan'
            books[int(form['book_id'].value)].user_id = int(form['user_id'].value)
            self.send_response(200)
            return 200
        except Exception as e:
            self.send_response(404)
            print 'Exception in get_loan: ', e

    def return_book(self, form):
        try:
            if books[int(form['book_id'].value)].status == 'available':
                self.send_response(404)
                return 404
            books[int(form['book_id'].value)].status = 'available'
            books[int(form['book_id'].value)].user_id = None
            self.send_response(200)
            return 200
        except Exception as e:
            self.send_response(404)
            print 'Exception in return_book: ', e

    def my_loans(self):
        try:
            parsed_path = urlparse.urlparse(self.path)
            query = parse_qs(parsed_path.query, keep_blank_values=True)
            if 'id' in query:
                my_loans = []
                for book in books:
                    if books[book].user_id == int(query['id'][0]) and books[book].status == 'on_loan':
                        my_loans.append(books[book])
                self.send_response(200)
                self.send_header("Content-type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps(my_loans, cls=LibraryEncoder))
                return
        except Exception as e:
            self.send_response(404)
            print 'Exception in my_loans: ', e

    def login(self, form):
        try:
            for user in users:
                if users[user].username == form['user'].value and users[user].password == form['password'].value:
                    self.send_response(200)
                    self.send_header("Content-type", "application/json")
                    self.end_headers()
                    self.wfile.write(user)
                    return 200
        except Exception as e:
            self.send_response(404)
            print 'Exception in login: ', e
        self.send_response(401)
        return 401


class LibraryEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, Books):
            return {"id": obj.id,
                    "author": obj.author,
                    "title": obj.title,
                    "year": obj.year,
                    "publishing": obj.publishing,
                    'status': obj.status,
                    'user_id': obj.user_id
                    }
        elif isinstance(obj, Users):
            return {"id": obj.id,
                    "username": obj.username,
                    "password": obj.password,
                    }
        return json.JSONEncoder.default(self, obj)


books = {}
users = {}


def populate():
    books[1] = Books(id=1, author='Roby', title='Sticazzi', year=1994, publishing='pubign', status='on_loan', user_id=1)
    books[2] = Books(id=2, author='Autore', title='Titolo', year=1994, publishing='pub', status='available',
                     user_id=None)
    users[1] = Users(id=1, username='nerdiken', password='123')
    users[2] = Users(id=2, username='piero', password='123')


def start_server():
    server_class = BaseHTTPServer.HTTPServer
    httpd = server_class((HOST_NAME, PORT_NUMBER), Handler)
    print time.asctime(), "Server Starts - %self:%self" % (HOST_NAME, PORT_NUMBER)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print time.asctime(), "Server Stops - %self:%self" % (HOST_NAME, PORT_NUMBER)


if __name__ == '__main__':
    populate()
    start_server()
