import json

from flask_restful import Resource, reqparse
import logging
from flask import Response, request


class CallLog(Resource):
    def post(self):
        data = request.get_json()
        file = open("callLog.json", "w")
        file.write(json.dumps(data,indent=4))
        return Response(status=200)