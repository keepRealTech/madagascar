from locust import HttpLocust, TaskSet, task, between, ResponseError
import json

class TestSet(TaskSet):

    token = ""

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
            "Authorization": "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTA0MDk4NTQsInVzZXJfbmFtZSI6IjAiLCJhdXRob3JpdGllcyI6WyJ1c2VyXzAiXSwianRpIjoiMzIyOGZlNTMtMjc5NC00ODRjLWJmM2MtMTAyMDMwZTRjODhmIiwiY2xpZW50X2lkIjoibGVtdXIiLCJzY29wZSI6WyJhbGwiXX0.uL0WiXW-1UHQaAaK20QEFnKXB6ShP8-_LI1GPUD3hos",
            "Content-Type": "application/json"
        }
   
        with self.client.get("/api/v1/feeds?islandId=6665811262355542016&fromHost=true", headers = headers, catch_response=True) as response:
            if json.loads(response.content)['rtn'] != 0:
                response.failure("failed")


class Run(HttpLocust):
    task_set = TestSet
    host = "https://islands.keepreal.cn"
    wait_time = between(1,5)
