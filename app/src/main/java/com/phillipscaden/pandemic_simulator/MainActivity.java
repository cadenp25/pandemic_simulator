package com.phillipscaden.pandemic_simulator;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText initial = findViewById(R.id.initial);
        final EditText size = findViewById(R.id.size);
        final EditText lethal = findViewById(R.id.lethality);
        final EditText infect = findViewById(R.id.infectivity);
        final EditText incubation = findViewById(R.id.incubation);
        final EditText average = findViewById(R.id.average);
        Button b = findViewById(R.id.button);
        final LineChart graph = findViewById(R.id.line);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = 100;
                int init = Integer.parseInt(initial.getText().toString());
                int s = Integer.parseInt(size.getText().toString());
                ;
                double i = Double.parseDouble(infect.getText().toString()) / 100.0;
                double l = Double.parseDouble(lethal.getText().toString()) / 100.0;
                double inc = Double.parseDouble(incubation.getText().toString());
                double avg = Double.parseDouble(average.getText().toString());
                Population p = new Population(init, s, i, l, inc, avg);
                p.progress(n);
                ArrayList<Float> deceased = p.getDeceased();
                ArrayList<Float> infected = p.getInfected();
                ArrayList<Float> healthy = p.getHealthy();
                ArrayList<Entry> three = new ArrayList<>();
                ArrayList<Entry> two = new ArrayList<>();
                ArrayList<Entry> one = new ArrayList<>();
                String[] x = new String[n];
                for(int it = 0; it < healthy.size(); it++)
                {
                    one.add(new Entry(healthy.get(it), it));
                    two.add(new Entry(infected.get(it), it));
                    three.add(new Entry(deceased.get(it), it));
                    x[it] = it+"";
                }


                ArrayList<ILineDataSet> lines = new ArrayList<>();

                LineDataSet h = new LineDataSet(one, "healthy");
                h.setDrawCircles(false);
                h.setColor(Color.GREEN);
                LineDataSet inf = new LineDataSet(two, "infected");
                inf.setDrawCircles(false);
                inf.setColor(Color.RED);
                LineDataSet d = new LineDataSet(three, "deceased");
                d.setDrawCircles(false);
                d.setColor(Color.BLACK);

                lines.add(h);
                lines.add(inf);
                lines.add(d);

                graph.setData(new LineData(x, lines));



            }
        });

    }


    public static class Individual {
        //Data
        public boolean infected = false;
        public boolean alive;
        public int date;
        public int incubation;

        //Constructors
        Individual(boolean i, double incubationPeriod) {
            if (i)
                infect(0);
            alive = true;
            double dif = incubationPeriod - (int) incubationPeriod;
            if (Math.random() > dif)
                incubation = (int) incubationPeriod;
            else
                incubation = (int) incubationPeriod + 1;
        }

        //Methods
        boolean isAlive() {
            return alive;
        }

        boolean isInfected() {
            return infected;
        }

        boolean isContagious(int day) {
            if (day - date >= incubation && isAlive() && isInfected())
                return true;
            return false;
        }

        void infect(int day) {
            date = day;
            infected = true;
        }

        void kill() {
            infected = false;
            alive = false;
        }

    }

    public static class Population {
        //Data
        public int populationSize;
        public int initialInfected;
        public double infectivity;
        public double lethality;
        public double incubationPeriod;
        public double averageContacts;
        public int numInfected;
        public int numDead;
        public ArrayList<Individual> people;
        public ArrayList<Float> deceased;
        public ArrayList<Float> infected;
        public ArrayList<Float> healthy;


        //Constructors
        Population(int initial, int size, double i, double l, double inc, double avg) {
            infected = new ArrayList<Float>();
            deceased = new ArrayList<Float>();
            healthy = new ArrayList<Float>();
            populationSize = size;
            infectivity = i;
            lethality = l;
            incubationPeriod = inc;
            averageContacts = avg;
            people = initialize(size, initial);
            numDead = 0;
            numInfected = initial;
        }

        //Methods
        ArrayList<Individual> initialize(int size, int initial) {
            ArrayList<Individual> ret = new ArrayList<Individual>();
            for (int it = 0; it < size; it++) {
                if (it < initial)
                    ret.add(new Individual(true, incubationPeriod));
                else
                    ret.add(new Individual(false, incubationPeriod));
            }
            return ret;
        }

        void progress(int n) {
            for (int it = 0; it < n; it++) {
                shuffle(it);
                store();
            }
        }

        void shuffle(int day) {
            for (Individual i : people) {
                if (i.isContagious(day)) // Should non-infected make contact?
                {
                    int con;
                    double dif = averageContacts - (int) averageContacts;
                    if (Math.random() > dif)
                        con = (int) averageContacts;
                    else
                        con = (int) averageContacts + 1;

                    for (int it = 0; it < con; it++) {
                        contact(i, people.get((int) (Math.random() * populationSize)), day);
                    }
                }
            }

            for (Individual i : people) {
                if (i.isContagious(day)) {
                    if (Math.random() <= lethality) {
                        i.kill();
                        numDead++;
                        numInfected--;
                    }
                }
            }
        }

        void contact(Individual i, Individual r, int day) {
            if (r.isAlive() && !r.isInfected()) {
                if (Math.random() <= infectivity) {
                    r.infect(day);
                    numInfected++;
                }
            }

        }

        void store() {
            healthy.add(Float.parseFloat((populationSize - (numDead + numInfected)) + ""));
            deceased.add(Float.parseFloat((numDead + "")));
            infected.add(Float.parseFloat((numInfected + "")));
        }


        ArrayList<Float> getHealthy() {
            return healthy;
        }

        ArrayList<Float> getInfected() {
            return infected;
        }

        ArrayList<Float> getDeceased() {
            return deceased;
        }
    }
}
