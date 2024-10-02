package co.casterlabs.caffeinated.app.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonNumber;

@JsonClass(exposeAll = true)
public class DashboardConfig {
    public List<JsonNumber> v = new LinkedList<>();
    public List<JsonNumber> h = new LinkedList<>();
    public Map<String, String> contents = Map.of("0,0", "welcomewagon");

}
