from locust import HttpLocust, TaskSet, task, between, ResponseError
import json

class TestSet(TaskSet):

    token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2MDAwNzY0ODksInVzZXJfbmFtZSI6IjY2NzEwNzAzMDcyNzg2NTEzOTIiLCJhdXRob3JpdGllcyI6WyJ1c2VyXzY2NzEwNzAzMDcyNzg2NTEzOTIiXSwianRpIjoiNWM3YTZhN2MtMWExMy00MzU0LTgzNzAtMjZhZjc4M2JlMDYxIiwiY2xpZW50X2lkIjoibGVtdXIiLCJzY29wZSI6WyJhbGwiXX0.bHC8CDsuVK6uyW-W1IwlDJfoSbKMnPyzhICgZ12dJn8"

  #  def on_start(self):
      #  headers = {
      #      "Content-Type": "application/json"
      #  }

      #  payload = {
      #      "loginType": "LOGIN_PASSWORD",
      #      "data": { 
      #          "username":"user",
      #          "password":"pass"
      #      }
      #  }

      #  with self.client.post("/api/v1/login", json.dumps(payload), headers = headers, catch_response = True) as response:
      #      self.token = json.loads(response.content)['data']['token']

    def getConfigs(self):
        self.client.get("/api/v1/configs?configType=IOS")
   
    def createFeed(self):
        headers = {
            "Authorization": "bearer " + self.token,
            "Content-Type": "application/json"
        }

        with self.client.post("/api/v1/feeds/6665820807295401984/reposts", json.dumps({ "content": "123", "isSuccessful": True }), headers = headers, catch_response=True) as response:
            if json.loads(response.content)['rtn'] != 0:
                response.failure("failed")
    @task
    def getFeed(self):
        headers = {
            "Authorization": "bearer " + self.token,
            "Content-Type": "application/json"
        }
   
        with self.client.get("/api/v1/notifications?type=COMMENTS", headers = headers, catch_response=True) as response:
            if json.loads(response.content)['rtn'] != 0:
                response.failure("failed")


class Run(HttpLocust):
    task_set = TestSet
    host = "https://islands.keepreal.cn"
    wait_time = between(0,0)
