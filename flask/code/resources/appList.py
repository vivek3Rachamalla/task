import json

from flask_restful import Resource
from flask import Response, request


class AppList(Resource):
    def post(self):
        data = request.get_json()
        file = open("appList.json", "w")
        file.write(json.dumps(data['apps'], indent=4))
        return Response(status=200)
