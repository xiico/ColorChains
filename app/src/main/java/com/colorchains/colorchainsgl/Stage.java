package com.colorchains.colorchainsgl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.parseInt;

/**
 * Created by francisco.cnmarao on 19/04/2017.
 */

public class Stage {
    public String id;
    public String name;
    public String[] tiles;
    public Integer targetScore;
    public Integer score;
    public Integer highScore;
    public Integer[] sets;
    public Integer moves;
    public Integer maxMoves;
    public Integer targetMoves;
    public int gemCount;

    public Stage(String jsonString) {
        try {
            JSONObject jStage = new JSONObject(jsonString);
            JSONArray jTiles = jStage.getJSONArray("tiles");
            JSONArray jSets = jStage.getJSONArray("sets");
            this.tiles = new String[jTiles.length()];
            for (int j = 0; j < jTiles.length(); j++) {
                this.tiles[j] = jTiles.get(j).toString();
            }
            this.id = jStage.getString("id");
            this.targetScore = parseInt(jStage.getString("targetScore"));
            this.score = parseInt(jStage.getString("score"));
            this.highScore =  parseInt(jStage.getString("highScore"));
            this.moves =  parseInt(jStage.getString("moves"));
            this.maxMoves =  parseInt(jStage.getString("maxMoves"));
            this.targetMoves =  parseInt(jStage.getString("targetMoves"));
            this.name =  jStage.getString("name");
            if(jSets.length() > 0) {
                this.sets = new Integer[jSets.length()];
                for (int j = 0; j < jSets.length(); j++) {
                    this.sets[j] = Integer.parseInt(jSets.get(j).toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public Stage(String id, String[] tiles, Integer targetScore, Integer score){

    }

    public static List<Stage> getStageList(String jsonString) {
        List<Stage> stageList = null;
        try {
            stageList = new ArrayList<>();
            JSONObject obj = new JSONObject(jsonString);
            JSONArray jsonArray = obj.getJSONArray("stages");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jStage = jsonArray.getJSONObject(i);
                Stage stage = new Stage(jStage.toString());
                stageList.add(stage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stageList;
    }
    public String toJsonString(String[] entities){
        String stage = String.format("{\"id\":\"%s\",\"name\":\"%s\" , \"tiles\":[" +
                        "\""+ entities[0] + "\"," +
                        "\""+ entities[1] + "\"," +
                        "\""+ entities[2] + "\"," +
                        "\""+ entities[3] + "\"," +
                        "\""+ entities[4] + "\"," +
                        "\""+ entities[5] + "\"," +
                        "\""+ entities[6] + "\"," +
                        "\""+ entities[7] + "\"]," +
                        "        \"customProperties\": [], \"targetScore\": %s, \"score\": %s, \"sets\":%s, \"highScore\":%s, \"moves\":%s, \"maxMoves\":%s, \"targetMoves\":%s }",
                this.id, this.name, this.targetScore, this.score, this.sets != null ? Arrays.toString(this.sets) : "[]", this.highScore, moves, this.maxMoves, this.targetMoves);
        return  stage;
    }
    public String toJsonString(){
        String stage = String.format("{\"id\":\"%s\",\"name\":\"%s\" , \"tiles\":[" +
                        "\""+ tiles[0] + "\"," +
                        "\""+ tiles[1] + "\"," +
                        "\""+ tiles[2] + "\"," +
                        "\""+ tiles[3] + "\"," +
                        "\""+ tiles[4] + "\"," +
                        "\""+ tiles[5] + "\"," +
                        "\""+ tiles[6] + "\"," +
                        "\""+ tiles[7] + "\"]," +
                        "        \"customProperties\": [], \"targetScore\": %s, \"score\": %s, \"sets\":%s, \"highScore\":%s }",
                this.id, this.name, this.targetScore, this.score, this.sets != null ? Arrays.toString(this.sets) : "[]", this.highScore, this.moves, this.maxMoves);
        return  stage;
    }
}
