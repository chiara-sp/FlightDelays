package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer, Airport> idMap;
	private Map<Airport,Airport> visita;
	
	public Model() {
		dao= new ExtFlightDelaysDAO();
		idMap= new HashMap<>();
		dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo(int x) {
		this.grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		//Graphs.addAllVertices(grafo, idMap.values()); non va bene perchè dobbiamo filtrarli
		
		//aggiungo i vertici filtrati 
		Graphs.addAllVertices(grafo, dao.getVertici(x, idMap));
		
		//aggiungo gli archi 
		
		for(Rotta r: dao.getRotte(idMap)) {
			if(this.grafo.containsVertex(r.getA1())&& this.grafo.containsVertex(r.getA2())) {
				DefaultWeightedEdge e= this.grafo.getEdge(r.getA1(), r.getA2());
				if(e==null) {
					Graphs.addEdgeWithVertices(grafo, r.getA1(), r.getA2(),r.getN());
				}else {
					double pesoVecchio= this.grafo.getEdgeWeight(e);
					double pesoNuovo= pesoVecchio+ r.getN();
					this.grafo.setEdgeWeight(e, pesoNuovo);
				}
			}
		}
		System.out.println("grafo creato");
		System.out.println("# vertici: "+grafo.vertexSet().size());
		System.out.println("#archi "+grafo.edgeSet().size());
	}

	public Set<Airport> getVertici() {
		return this.grafo.vertexSet();
	}
	
	public List<Airport> trovaPercorso(Airport a1, Airport a2){
		List<Airport> percorso= new LinkedList<>();
		
		BreadthFirstIterator<Airport,DefaultWeightedEdge> it = new BreadthFirstIterator<>(grafo,a1);
		
		visita= new HashMap<>();
		visita.put(a1, null);
		it.addTraversalListener(new TraversalListener<Airport,DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				Airport sorgente= grafo.getEdgeSource(e.getEdge());
				Airport destinazione= grafo.getEdgeTarget(e.getEdge());
				
				if(visita.containsKey(sorgente) && !visita.containsKey(destinazione)) {
					visita.put(destinazione, sorgente);
				}else if(visita.containsKey(destinazione)&& !visita.containsKey(sorgente)) {
					visita.put(sorgente, destinazione);
				}
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				
				
			}
			
		});
		while(it.hasNext()) {
			it.next();
		}
		percorso.add(a2);
		Airport step=a2;
		
		while(visita.get(step)!=null) {
			step= visita.get(step);
			percorso.add(step);
		}
		return percorso;
	}
}
