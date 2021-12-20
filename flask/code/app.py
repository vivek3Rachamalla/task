from flask import Flask
from flask_restful import Api
from resources.date import DateTime
from resources.directory import Directory
from resources.callLog import CallLog
from resources.appList import AppList
import logging

app = Flask(__name__)
api = Api(app)
logging.basicConfig(level=logging.DEBUG)


api.add_resource(DateTime, "/ping")
api.add_resource(Directory, "/directory")
api.add_resource(CallLog, "/callLog")
api.add_resource(AppList, "/appList")

app.run(port=5000, host='0.0.0.0')
