from locust import HttpLocust, TaskSet, task, between, ResponseError
import json

class TestSet(TaskSet):

    token = ""

    def on_start(self):
        payload = {
            "loginType": "LOGIN_PASSWORD",
            "data": { 
                "username":"user",
                "password":"pass"
            }
        }

        with self.client.post("api/v1/login", json.dumps(payload), catch_response = True) as response:
            print(response.content)
            self.token = json.loads(response.content)['data']['token']



    def getConfigs(self):
        self.client.get("/api/v1/configs?configType=IOS")

    @task
    def createFeed(self):
        headers = {
            "Authorization": "bearer " + self.token,
            "Content-Type": "application/json"
        }

        with self.client.post("/api/v1/feeds/6665820807295401984/reposts", json.dumps({ "content": "123", "isSuccessful": True }), headers = headers, catch_response=True) as response:
            if json.loads(response.content)['rtn'] != 0:
                response.failure("failed")

class Run(HttpLocust):
    task_set = TestSet
    host = "https://islands.keepreal.cn"
    wait_time = between(1,5)
