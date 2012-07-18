package com.conref.sootUtil.graph;

import java.util.*;

/**
 * Defines the notion of a directed graph.
 * 
 * @param N
 *            node type
 */
public class UndirectedGraph<N> implements Iterable<N> {
	private Map<N, Set<N>> _node2neighbors = new HashMap<N, Set<N>>();

	/**
	 * Returns a list of predecessors for the given node in the graph.
	 */
	public void add(N node, Collection<N> neighbors) {
		Set<N> neighborNodes = getNeighborSet(node);
		neighborNodes.addAll(neighbors);

		for (N to : neighbors) {
			neighborNodes = getNeighborSet(to);
			neighborNodes.add(node);
		}
	}

	private Set<N> getNeighborSet(N node) {
		Set<N> neighborNodes = _node2neighbors.get(node);
		if (neighborNodes == null) {
			neighborNodes = new HashSet<N>();
			_node2neighbors.put(node, neighborNodes);
		}

		return neighborNodes;
	}

	public Collection<N> getNeighbors(N node) {
		return _node2neighbors.get(node);
	}

	/**
	 * Returns the node count for this graph.
	 */
	public int size() {
		return _node2neighbors.keySet().size();
	}

	/**
	 * Returns an iterator for the nodes in this graph. No specific ordering of
	 * the nodes is guaranteed.
	 */
	public Iterator<N> iterator() {
		return _node2neighbors.keySet().iterator();
	}

	public Collection<Collection<N>> findConnectedComponents() {
		Set<N> processed = new HashSet<N>();
		Collection<Collection<N>> groups = new HashSet<Collection<N>>();

		for (N node : _node2neighbors.keySet()) {
			if (!processed.contains(node)) {
				Collection<N> reach = reachFrom(node, processed);
				groups.add(reach);
			}
		}

		return groups;
	}

	private Collection<N> reachFrom(N node, Set<N> processed) {
		Stack<N> stack = new Stack<N>();
		Collection<N> reach = new HashSet<N>();
		stack.add(node);

		while (!stack.isEmpty()) {
			node = stack.pop();
			reach.add(node);
			processed.add(node);

			Collection<N> neighbors = _node2neighbors.get(node);
			for (N to : neighbors) {
				if (!processed.contains(to)) {
					stack.push(to);
				}
			}
		}

		return reach;
	}
}
