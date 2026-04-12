-- Create Player table
CREATE TABLE IF NOT EXISTS player (
    player_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Create Team table
CREATE TABLE IF NOT EXISTS team (
    team_id BIGSERIAL PRIMARY KEY,
    offense_id BIGINT NOT NULL,
    defense_id BIGINT NOT NULL,
    team_color VARCHAR(1),
    FOREIGN KEY (offense_id) REFERENCES player(player_id),
    FOREIGN KEY (defense_id) REFERENCES player(player_id)
);

-- Create Matchup table (historical scores between two player pairs, regardless of position)
CREATE TABLE IF NOT EXISTS matchup (
    matchup_id BIGSERIAL PRIMARY KEY,
    player1a_id BIGINT NOT NULL,
    player1b_id BIGINT NOT NULL,
    player2a_id BIGINT NOT NULL,
    player2b_id BIGINT NOT NULL,
    pair1_wins INTEGER NOT NULL DEFAULT 0,
    pair2_wins INTEGER NOT NULL DEFAULT 0,
    draws INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (player1a_id) REFERENCES player(player_id),
    FOREIGN KEY (player1b_id) REFERENCES player(player_id),
    FOREIGN KEY (player2a_id) REFERENCES player(player_id),
    FOREIGN KEY (player2b_id) REFERENCES player(player_id),
    UNIQUE(player1a_id, player1b_id, player2a_id, player2b_id)
);

-- Create Match table
CREATE TABLE IF NOT EXISTS match (
    match_id BIGSERIAL PRIMARY KEY,
    match_date DATE NOT NULL,
    team1_id BIGINT NOT NULL,
    team2_id BIGINT NOT NULL,
    team1_game_score INTEGER NOT NULL,
    team2_game_score INTEGER NOT NULL,
    FOREIGN KEY (team1_id) REFERENCES team(team_id),
    FOREIGN KEY (team2_id) REFERENCES team(team_id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_match_date ON match(match_date);
CREATE INDEX IF NOT EXISTS idx_match_team1 ON match(team1_id);
CREATE INDEX IF NOT EXISTS idx_match_team2 ON match(team2_id);
CREATE INDEX IF NOT EXISTS idx_matchup_player1a ON matchup(player1a_id);
CREATE INDEX IF NOT EXISTS idx_matchup_player1b ON matchup(player1b_id);
CREATE INDEX IF NOT EXISTS idx_matchup_player2a ON matchup(player2a_id);
CREATE INDEX IF NOT EXISTS idx_matchup_player2b ON matchup(player2b_id);
CREATE INDEX IF NOT EXISTS idx_team_offense ON team(offense_id);
CREATE INDEX IF NOT EXISTS idx_team_defense ON team(defense_id);
CREATE INDEX IF NOT EXISTS idx_player_name ON player(name);

