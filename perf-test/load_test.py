from locust import HttpUser, task, between, events

class ChessGamePlayerIncrementalLoadTest(HttpUser):
    wait_time = between(1, 5)

    @task
    def get_game_players(self):
        target_user_count = int(self.environment.parsed_options.user_count)
        if (self.environment.runner.user_count <= target_user_count):
            self.client.get(f"/api/v1/games", name="fetch chess game players")
        else:
            print('Target user count reached. Exiting...')
            self.environment.runner.quit()

    def on_start(self):
        self.environment.events.quitting.add_listener(self.on_quit)

    def on_quit(self):
        print('Performing cleanup before quitting...')


# Add a custom CLI argument
@events.init_command_line_parser.add_listener
def on_add_command_line_parser(parser):
    parser.add_argument(
        "--user-count",
        type=int,
        default=500,  # Default user count
        help="Specify the target user count from the CLI.",
    )

@events.init.add_listener
def on_locust_init(environment, **kwargs):
    @environment.events.quitting.add_listener
    def on_global_quitting(environment, **kwargs):
        print("Locust is shutting down globally...")
