import os
from google.appengine.ext.webapp import template
from urlparse import urlparse
from google.appengine.ext import ndb
import logging
import webapp2
import json
import urllib
import urllib2
import uuid
from google.appengine.api import urlfetch


CLIENT_ID = '218878733303-0mlgrdrl8o8cpol9rs2lrnifp0kenuqm.apps.googleusercontent.com'
CLIENT_SECRET = 'Jwcoeid9qAIGQAntY1De0mG5'
REDIRECT_URI = 'https://oauthdemo-166515.appspot.com/oauth' 

class OAuthHandler(webapp2.RequestHandler):
	def get(self):
		logging.debug('The contents of the GET request are: '+ repr(self.request.GET))
		access_code = self.request.GET['code']
		returned_state = self.request.GET['state']
		post_params = {'code':access_code, 'client_id':CLIENT_ID, 'client_secret':CLIENT_SECRET, 'redirect_uri':REDIRECT_URI, 'grant_type':'authorization_code'}
		head = {'Content-Type': 'application/x-www-form-urlencoded'}
		encode_post = urllib.urlencode(post_params)
		result=urlfetch.fetch('https://www.googleapis.com/oauth2/v4/token', headers=head, payload=encode_post, method=urlfetch.POST)
		json_result=json.loads(result.content)
		accessToken=json_result['access_token']
		#
		#use token to access user details
		tokenHeaders = { 'Authorization' : "Bearer " + accessToken}
		request = urllib2.Request("https://www.googleapis.com/plus/v1/people/me", headers=tokenHeaders)
		contents = urllib2.urlopen(request).read()
		json_contents = json.loads(contents)
		name = json_contents['name']
		firstName = name['givenName']
		lastName = name['familyName']
		url = json_contents['url']
		#
		#output results
		template_values = {'firstName': firstName, 'lastName': lastName, 'url': url, 'state': returned_state}
		self.response.out.write(template.render('display.html',template_values))


class MainPage(webapp2.RequestHandler):
	def get(self):
		self.response.out.write(template.render('index.html', {}))

class LogInHandler(webapp2.RequestHandler):
	def get(self):
		def gen_url():
			state = getState()
			getParams = { 'response_type':'code', 'state':state, 'scope':'email', 'redirect_uri':REDIRECT_URI, 'client_id':CLIENT_ID}
			ordered_params = []
			ordered_params.append('response_type')
			ordered_params.append('client_id')
			ordered_params.append('redirect_uri')
			ordered_params.append('scope')
			ordered_params.append('state')
			query = "https://accounts.google.com/o/oauth2/v2/auth?" + "&".join([item+'='+urllib.quote_plus(getParams[item]) for item in ordered_params])
			#url = "https://accounts.google.com/o/oauth2/v2/auth?" + urllib.urlencode(getParams)
			url = urllib.unquote(query)
			return url
		url = gen_url()
		#self.response.write(url)
		content = urllib2.urlopen(url).read()
		self.response.write(content)

def getState():
	state = str(uuid.uuid4())
	return state

app = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/oauth', OAuthHandler),
    ('/login', LogInHandler)
], debug=True)

def main():
	application.run()

if __name__ == "__main__":
	main()