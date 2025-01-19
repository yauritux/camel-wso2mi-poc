from locust import HttpUser, task, between, events
import time
import threading


class ChessGamePlayerIncrementalLoadTest(HttpUser):
    wait_time = between(1, 5)

    @task
    def get_game_players(self):
        self.client.get(f"/api/v1/games", name="/api/v1/games")
        # self.environment.runner.quit() # necessary to stop the user after making one request

    @events.init.add_listener
    def on_locust_init(environment, **kwargs):
        environment.runner.start(100, spawn_rate=100)
        threading.Thread(target=increment_users, args=(environment,)).start()


    # def on_start(self):
    #     self.environment.events.quitting.add_listener(self.on_quit)

    # def on_quit(self):
    #     self.stop(True)
    #     self.environment.runner.quit()

def increment_users(environment):
    current_users = 100
    target_users = 1000
    increment = 100
    interval = 60 # 1 minute in seconds

    while current_users < target_users:
        time.sleep(interval)
        environment.runner.spawn_users(increment, wait=True)
        current_users += increment

    # Stop the test after reaching the target number of users
    environment.runner.quit()
