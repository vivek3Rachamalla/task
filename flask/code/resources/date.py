from flask_restful import Resource, reqparse
import logging
from flask import Response


class DateTime(Resource):
    parser = reqparse.RequestParser()
    parser.add_argument('pingString',
                        type=str,
                        required=True,
                        help='date field is required')

    def post(self):
        data = DateTime.parser.parse_args()
        logging.debug(data['pingString'])
        return Response(status=200)
