package basic.linuxNativeHelloWorld;

import java.util.HashMap;
import java.util.Map;

import org.mt4j.AbstractMTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.bounds.IBoundingShape;
import org.mt4j.components.visibleComponents.shapes.MTEllipse;
import org.mt4j.components.visibleComponents.widgets.MTOverlayContainer;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTDevInputEvt;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputProcessors.globalProcessors.AbstractGlobalInputProcessor;
import org.mt4j.sceneManagement.Iscene;
import org.mt4j.util.MTColor;
import org.mt4j.util.PlatformUtil;
import org.mt4j.util.math.Vector3D;

import processing.core.PApplet;

public class MTDevCursorTracer extends AbstractGlobalInputProcessor {
	
	/** The app. */
	private AbstractMTApplication app;
	
	/** The cursor id to display shape. */
	private Map<InputCursor, CursorEllipse> cursorIDToDisplayShape;
	
	/** The scene. */
	private Iscene scene;
	
	/** The overlay group. */
	private MTComponent overlayGroup;
	
	private float ellipseDefaultRadius = 15;

	public MTDevCursorTracer(AbstractMTApplication mtApp, Iscene currentScene) {
		this.app = mtApp;
		this.scene = currentScene;
		this.cursorIDToDisplayShape = new HashMap<InputCursor, CursorEllipse>();
		
		if (PlatformUtil.isAndroid()){
			ellipseDefaultRadius = 30;
		}
		
		this.overlayGroup = new MTOverlayContainer(app, "Cursor Trace group");
		mtApp.invokeLater(new Runnable() {
			public void run() {
				scene.getCanvas().addChild(overlayGroup);
			}
		});
	}
	
	/**
	 * Creates the display component.
	 * 
	 * @param applet the applet
	 * @param position the position
	 * @param radiusY2 
	 * 
	 * @return the abstract shape
	 */
	protected CursorEllipse createDisplayComponent(PApplet applet, Vector3D position, float orientation, float radiusX, float radiusY){
		CursorEllipse displayShape = new CursorEllipse(applet, position, radiusX, radiusY, 30);
		displayShape.rotateZ(displayShape.getCenterOfMass2DLocal(), 90*orientation);
		displayShape.setPickable(false);
		displayShape.setNoFill(true);
		displayShape.setDrawSmooth(true);
		displayShape.setStrokeWeight(2);
		displayShape.setStrokeColor(new MTColor(100, 130, 220, 255));
		return displayShape;
	}
	
	private class CursorEllipse extends MTEllipse{
		public CursorEllipse(PApplet applet, Vector3D centerPoint, float radiusX, float radiusY, int segments) {
			super(applet, centerPoint, radiusX, radiusY, segments);
		}
		@Override
		protected IBoundingShape computeDefaultBounds() {
			return null;
		}
		@Override
		protected void setDefaultGestureActions() {
			//Dont need gestures
		}
	}
	
	

	/* (non-Javadoc)
	 * @see org.mt4j.input.inputProcessors.globalProcessors.AbstractGlobalInputProcessor#processInputEvtImpl(org.mt4j.input.inputData.MTInputEvent)
	 */
	@Override
	public void processInputEvtImpl(MTInputEvent inputEvent) {
		if (inputEvent instanceof AbstractCursorInputEvt) {
			AbstractCursorInputEvt cursorEvt = (AbstractCursorInputEvt)inputEvent;
			InputCursor c = ((AbstractCursorInputEvt)inputEvent).getCursor();
			Vector3D position = new Vector3D(cursorEvt.getX(), cursorEvt.getY());
			
			float orientation = 0;
			float radiusX = ellipseDefaultRadius;
			float radiusY = ellipseDefaultRadius;
			if (cursorEvt instanceof MTDevInputEvt) {
				orientation = ((MTDevInputEvt) cursorEvt).getOrientationTouch();
				radiusX = ((MTDevInputEvt) cursorEvt).getMajorTouch() / 2;
				radiusY = ((MTDevInputEvt) cursorEvt).getMinorTouch() / 2;
			}

			CursorEllipse displayShape = null;
			switch (cursorEvt.getId()) {
			case AbstractCursorInputEvt.INPUT_STARTED:
				displayShape = createDisplayComponent(app, position, 0, radiusX, radiusY);
				cursorIDToDisplayShape.put(c, displayShape);
				overlayGroup.addChild(displayShape);
				displayShape.setPositionGlobal(position);
				
//				compToCreationTime.put(displayShape, System.currentTimeMillis()); //FIXME REMOVE
//				displayShape.setUserData("Cursor", c);//FIXME REMOVE
				break;
			case AbstractCursorInputEvt.INPUT_UPDATED:
				displayShape = cursorIDToDisplayShape.get(c);
				if (displayShape != null){
					CursorEllipse tmpShape = createDisplayComponent(app, position, orientation, radiusX, radiusY);
//					displayShape.setPositionGlobal(position);
					displayShape.setVertices(tmpShape.getVerticesGlobal());
				}
				break;
			case AbstractCursorInputEvt.INPUT_ENDED:
				displayShape = cursorIDToDisplayShape.remove(c);
				if (displayShape != null){
					displayShape.destroy();
				}
				break;
			default:
				break;
			}
		}
	}
}
