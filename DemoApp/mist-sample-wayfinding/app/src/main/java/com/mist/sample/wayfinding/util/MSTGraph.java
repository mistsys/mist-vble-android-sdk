package com.mist.sample.wayfinding.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MSTGraph {
    private Queue queue;
    private ConcurrentHashMap<String, JSONObject> vertices;
    private ConcurrentHashMap<String, Float> dist;
    private ConcurrentHashMap<String, String> prev;

    public MSTGraph(){
        this.queue = new Queue();
        this.vertices = new ConcurrentHashMap<String, JSONObject>();
        this.dist = new ConcurrentHashMap<String, Float>();
        this.prev = new ConcurrentHashMap<String, String>();
    }

    public void addVertex(String nodeName, JSONObject edges){
        this.vertices.put(nodeName, edges);
    }

    public ArrayList<String> findPathFrom(String start, String end){
        ArrayList<String> path = null;

        for (Map.Entry<String, JSONObject> e : this.vertices.entrySet()){
            String nodeName = e.getKey();
            if (nodeName.equals(start)){
                this.dist.put(nodeName, 0f);
                this.prev.put(nodeName, "");
                this.queue.enqueue(new QueueItem(nodeName, 0f));
            } else {
                this.dist.put(nodeName,Float.POSITIVE_INFINITY);
                this.prev.put(nodeName, "");
                this.queue.enqueue(new QueueItem(nodeName,Float.POSITIVE_INFINITY));
            }
        }

        while(!this.queue.isEmpty()) {
            QueueItem node = this.queue.dequeue();
            String smallest = node.getName();
            if (smallest.equals(end)){
                path = new ArrayList<String>();
                path.add(smallest);
                while (this.prev.get(smallest) != ""){
                    smallest = this.prev.get(smallest);
                    path.add(smallest);
                }
                return path;
            }

            JSONObject edges = this.vertices.get(smallest);
            Iterator keys = edges.keys();

            while(keys.hasNext()) {
                String neighbor = (String)keys.next();
                try{
                    Float alt = this.dist.get(smallest)+Integer.valueOf((String)edges.get(neighbor));
                    System.out.println("neighbor weight: " + edges.get(neighbor));
                    System.out.println("neighbor: " + neighbor);
                    System.out.println("neighbor: " + this.dist.get(neighbor));
                    System.out.println("alt: " + alt);
                    System.out.println("check: " + (alt < this.dist.get(neighbor)) + "\n\n");

                    if (alt < this.dist.get(neighbor)){
                        this.dist.put(neighbor,alt);
                        this.prev.put(neighbor,smallest);
                        this.queue.enqueue(new QueueItem(neighbor, alt));
                    }
                } catch (JSONException e){
                    System.out.println("JSONException: " + e.getMessage());
                }
            }
        }
        return path;
    }

    public class QueueItem implements Comparable {
        public String name;
        public Float score;

        public QueueItem(String name, Float score){
            this.name = name;
            this.score = score;
        }

        public String getName(){
            return this.name;
        }

        public Float getScore(){
            return this.score;
        }

        @Override
        public String toString(){
            return "(" + this.name + "," + this.score + ")";
        }

        @Override
        public int compareTo(Object o) {
            QueueItem qi = (QueueItem)o;
            return this.getScore().compareTo(qi.getScore());
        }
    }

    public class Queue{
        ArrayList <QueueItem> _queue;

        public Queue(){
            this._queue = new ArrayList<>();
        }

        public void enqueue(QueueItem item){
            this._queue.add(item);
            Collections.sort(this._queue);
        }

        public QueueItem dequeue(){
            QueueItem smallest = this._queue.get(0);
            this._queue.remove(0);
            return smallest;
        }

        public boolean isEmpty(){
            return (this._queue.size() == 0);
        }

        @Override
        public String toString() {
            String x = "";
            Iterator i = this._queue.iterator();
            while (i.hasNext()){
                QueueItem item = (QueueItem) i.next();
                x += "(" + item.getName() + "," + item.getScore() + ")";
            }
            return x;
        }
    }
}
