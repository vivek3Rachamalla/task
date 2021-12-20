from flask_restful import Resource, reqparse
from flask import Response, request
import logging


class Directory(Resource):
    parser = reqparse.RequestParser()
    parser.add_argument('directories',
                        action='append',
                        required=True,
                        help='date field is required')
    parser.add_argument('downloadFiles',
                        action='append',
                        required=True,
                        help='date field is required')

    def post(self):
        data = Directory.parser.parse_args()
        logging.debug(data)
        return Response(status=200)
