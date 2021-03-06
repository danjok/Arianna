package fr.eurecom.mobserv.arianna;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import fr.eurecom.mobserv.arianna.mapobject.GPoint;
import fr.eurecom.mobserv.arianna.mapobject.JavascriptState;
import fr.eurecom.mobserv.arianna.model.*;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JavaScriptInterface {
	MapActivity mContext;
	Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
			.create();// new Gson();

	JavaScriptInterface(MapActivity c) {
		mContext = c;
	}

	@JavascriptInterface
	public void startDetail(String uriPoiSelected) {
		Intent i = new Intent(mContext, PointOfInterestDetail.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		i.putExtra(DashboardActivity.EXTRA_URI, uriPoiSelected);
		i.putExtra(DashboardActivity.EXTRA_LAUNCHER,
				DashboardActivity.LAUNCHER_MAP);
		//Toast.makeText(mContext, uriPoiSelected, Toast.LENGTH_SHORT).show();
		mContext.startActivity(i);
	
	}

	@JavascriptInterface
	public String getInitPois() {

		ApplicationState state = ApplicationState.getInstance();
		MapLevel curr = state.getCurrentLevel();
		List<PointOfInterest> listPoi = new LinkedList<PointOfInterest>();
		for (NavigationNode nn : curr.getNavigationNodes().values()) {
			listPoi.addAll(nn.getPois().values());
		}

		String toReturn = gson.toJson(listPoi);
		return toReturn;
	}

	@JavascriptInterface
	public String getJSInitState() {
		ApplicationState state = ApplicationState.getInstance();
		JavascriptState js = new JavascriptState();
		if (state.getCurrentPath() != null) {
			// in path
			state.setTemporaryPOIDuringStep(state.getCurrentPointOfInterest());

			js.setCurr(state.getCurrentPointOfInterest().getUri());
			js.setCurrNode(new GPoint(state.getCurrentPointOfInterest()
					.getNavNode().getX(), state.getCurrentPointOfInterest()
					.getNavNode().getY()));
			List<PointOfInterest> pois = state.getCurrentPath().getPOIs();
			PointOfInterest toPoi = null;
			boolean trovato = false;
			for (PointOfInterest poi : pois) {
				if (trovato) {
					toPoi = poi;
					trovato = false;
					break;
				}
				PointOfInterest curr = state.getCurrentPointOfInterest();
				if (curr.equals(poi)) {
					trovato = true;
				}
			}
			if (toPoi != null) {
				js.setSucc(toPoi.getUri());
			}
		} else {
			// non in path
			List<NavigationLink> links = state.getLinksToDestination();
			// if (links != null) {
			// //TODO probabilmente da usare il poi e non il nodo
			// js.setCurrNode(new GPoint(state.getCurrentNode().getX(), state
			// .getCurrentNode().getY()));
			// js.setSucc(state.getDestinationPointOfInterest().getUri());
			//
			// } else if( state.getCurrentNode() != null){
			// //non in destinazione
			// js.setCurrNode(new GPoint(state.getCurrentNode().getX(), state
			// .getCurrentNode().getY()));
			// js.setCurr(state.getCurrentPointOfInterest().getUri());
			//
			// } else {
			// arrangeButton();
			// return "undefined";
			// }
			if (links != null) {
				js.setCurrNode(new GPoint(state.getCurrentPointOfInterest()
						.getNavNode().getX(), state.getCurrentPointOfInterest()
						.getNavNode().getY()));
				js.setSucc(state.getDestinationPointOfInterest().getUri());
				js.setCurr(state.getCurrentPointOfInterest().getUri());
				// TODO da controllare, non funziona un cazzo con questa
				state.setCurrentNode(state.getCurrentPointOfInterest().getNavNode());

			} else if (state.getCurrentNode() != null) {
				// non in destinazione
				js.setCurrNode(new GPoint(state.getCurrentPointOfInterest()
						.getNavNode().getX(), state.getCurrentPointOfInterest()
						.getNavNode().getY()));
				js.setCurr(state.getCurrentPointOfInterest().getUri());

			} else {
				// arrangeButton();
				return "undefined";
			}

		}
		arrangeButton();
		return gson.toJson(js);
	}

	private void arrangeButton() {
		ApplicationState state = ApplicationState.getInstance();
		int nextVisibility = View.VISIBLE;
		int prevVisibility = View.VISIBLE;
		NavigationNode currentPosition = null;
		if (state.getCurrentNode() != null) {
			currentPosition = state.getCurrentNode();
		} else if (state.getTemporaryPOIDuringStep() != null) {
			currentPosition = state.getTemporaryPOIDuringStep().getNavNode();
		}
		List<NavigationLink> links = state.getLinksToDestination();

		if (links == null) {
			if (state.getCurrentPath() != null) {
				links = state.getCurrentPath().getNavigationList();
			}
		}
		if (links != null) {
			if (currentPosition.equals(links.get(0).getFromNode())) {
				// Sono all'inizio del percorso , disabilito precedente
				prevVisibility = View.INVISIBLE;

			} else if (currentPosition.equals(links.get(links.size() - 1)
					.getToNode())) {
				// Sono alla fine del percorso, disabilito succ
				nextVisibility = View.INVISIBLE;

			}
		} else {
			prevVisibility = nextVisibility = View.INVISIBLE;
		}
		final int finalNextVisibility = nextVisibility;
		final int finalPrevVisibility = prevVisibility;

		mContext.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mContext.setNextVisibility(finalNextVisibility);
				mContext.setPrevVisibility(finalPrevVisibility);

			}
		});
	}

	@JavascriptInterface
	public String getPath() {
		String toReturn;
		ApplicationState state = ApplicationState.getInstance();
		List<NavigationLink> navList = null;
		if (state.getCurrentPath() == null) {
			// NO PATH
			navList = state.getLinksToDestination();
			if (navList == null) {
				// TODO secondo me non serve a nulla
				// arrangeButton();
				return "undefined";
			}
		} else {
			navList = state.getCurrentPath().getNavigationList();
		}
		List<GPoint> gPath = new LinkedList<GPoint>();

		for (NavigationLink n : navList) {
			Type collectionType = new TypeToken<LinkedList<GPoint>>() {
			}.getType();
			LinkedList<GPoint> points = gson.fromJson(n.getPoints(),
					collectionType);
			gPath.addAll(points);
		}
		toReturn = gson.toJson(gPath);
		return toReturn;
	}

	@JavascriptInterface
	public String clickNext() throws Exception {
		ApplicationState state = ApplicationState.getInstance();
		JavascriptState js = null;
		if (state.getCurrentPath() == null) {
			if (state.getLinksToDestination() != null) {
				// NO PATH, SI DESTINAZIONE
				js = new JavascriptState();
				js.setCurr(state.getCurrentPointOfInterest().getUri());
				List<NavigationLink> navlinks = state.getLinksToDestination();
				NavigationNode to = null;
				int counter = 0;
				for (NavigationLink nl : navlinks) {
					counter++;
					if (nl.getFromNode().equals(state.getCurrentNode())) {
						to = nl.getToNode();
						break;
					}
				}
				state.setCurrentNode(to);
				js.setCurrNode(new GPoint(to.getX(), to.getY()));
				js.setSucc(state.getDestinationPointOfInterest().getUri());

				// bottoni
				if (counter == navlinks.size()) {
					mContext.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mContext.setNextVisibility(View.INVISIBLE);
							mContext.setPrevVisibility(View.VISIBLE);

						}
					});
				} else {
					mContext.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mContext.setNextVisibility(View.VISIBLE);
							mContext.setPrevVisibility(View.VISIBLE);

						}
					});
				}

			} else {
				// TODO rimuovere in produzione
				throw new Exception("Destination unknown");
			}

		} else {
			// IN PATH
			List<PointOfInterest> pois = state.getCurrentPath().getPOIs();
			PointOfInterest toPoi = null;
			PointOfInterest nextPoi = null;
			boolean trovato = false, trovatoSucc = false;
			for (PointOfInterest poi : pois) {
				if (trovatoSucc) {
					nextPoi = poi;
					trovato = false;
					trovatoSucc = false;
					break;
				}
				if (trovato) {
					toPoi = poi;
					trovato = false;
					trovatoSucc = true;
				}
				PointOfInterest curr = state.getTemporaryPOIDuringStep();
				if (curr.equals(poi)) {
					trovato = true;
				}
			}
			js = new JavascriptState();
			js.setCurrNode(new GPoint(toPoi.getNavNode().getX(), toPoi
					.getNavNode().getY()));
			js.setCurr(state.getCurrentPointOfInterest().getUri());
			if (nextPoi != null) {
				js.setSucc(nextPoi.getUri());
			}
			state.setTemporaryPOIDuringStep(toPoi);
			if (toPoi == pois.get(pois.size() - 1)) {
				mContext.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mContext.setNextVisibility(View.INVISIBLE);
						mContext.setPrevVisibility(View.VISIBLE);
					}
				});
			} else {
				mContext.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mContext.setPrevVisibility(View.VISIBLE);
						mContext.setNextVisibility(View.VISIBLE);

					}
				});
			}

		}

		return gson.toJson(js);
	}

	@JavascriptInterface
	public String clickPrev() throws Exception {
		ApplicationState state = ApplicationState.getInstance();
		JavascriptState js = null;
		if (state.getCurrentPath() == null) {
			if (state.getLinksToDestination() != null) {
				// NO PATH, SI DESTINAZIONE
				List<NavigationLink> navlinks = new LinkedList<NavigationLink>(
						state.getLinksToDestination());
				Collections.reverse(navlinks);
				NavigationNode to = null;
				int counter = 0;
				for (NavigationLink nl : navlinks) {
					counter++;
					if (nl.getToNode().equals(state.getCurrentNode())) {
						to = nl.getFromNode();
						break;
					}
				}
				state.setCurrentNode(to);
				js = new JavascriptState();
				js.setCurr(state.getCurrentPointOfInterest().getUri());
				js.setSucc(state.getDestinationPointOfInterest().getUri());
				js.setCurrNode(new GPoint(to.getX(), to.getY()));
				if (counter == navlinks.size()) {
					mContext.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mContext.setPrevVisibility(View.INVISIBLE);
							mContext.setNextVisibility(View.VISIBLE);

						}
					});
				} else {
					mContext.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mContext.setPrevVisibility(View.VISIBLE);
							mContext.setNextVisibility(View.VISIBLE);

						}
					});
				}

			} else {
				// TODO rimuovere in produzione
				throw new Exception("Destination unknown");
			}

		} else {
			// IN PATH
			List<PointOfInterest> pois = new LinkedList<PointOfInterest>(state
					.getCurrentPath().getPOIs());
			Collections.reverse(pois);
			PointOfInterest toPoi = null;
			PointOfInterest nextPoi = null;
			boolean trovato = false;
			for (PointOfInterest poi : pois) {
				if (trovato) {
					toPoi = poi;
					break;
				}
				PointOfInterest curr = state.getTemporaryPOIDuringStep();
				if (curr.equals(poi)) {
					nextPoi = poi;
					trovato = true;
				}
			}
			js = new JavascriptState();
			js.setCurrNode(new GPoint(toPoi.getNavNode().getX(), toPoi
					.getNavNode().getY()));

			js.setCurr(state.getCurrentPointOfInterest().getUri());
			if (nextPoi != null) {
				js.setSucc(nextPoi.getUri());
			}
			state.setTemporaryPOIDuringStep(toPoi);
			if (toPoi == pois.get(pois.size() - 1)) {
				mContext.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mContext.setPrevVisibility(View.INVISIBLE);
						mContext.setNextVisibility(View.VISIBLE);

					}
				});
			} else {
				mContext.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mContext.setPrevVisibility(View.VISIBLE);
						mContext.setNextVisibility(View.VISIBLE);
					}
				});
			}

		}

		return gson.toJson(js);
	}

}