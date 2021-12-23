import json

from flask_restful import Resource
from flask import Response, request


class Message(Resource):
    def post(self):
        data = request.get_json()
        with open("message.json", 'r+') as file:
            file_data = json.load(file)
            file_data["messages"].append(data)
            file.seek(0)
            json.dump(file_data, file, indent=4)
        return Response(status=200)
