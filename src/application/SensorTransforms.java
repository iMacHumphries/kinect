package application;

import math.Matrix4f;
import math.Vector3f;
import math.Vector4f;

public class SensorTransforms {
	
	public static Vector3f sensorOffsetFromScreenCenter = new Vector3f(0, 0, 0);
	public static float sensorElevationAngleToUse = 0.0f;
	
    /// <summary>
    /// Creates a transform matrix that transforms points from sensor skeleton space to screen coordinates
    /// </summary>
    /// <param name="sensorOffset">vector, in meters, from the center of the display to the center of the Kinect sensor</param>
    /// <param name="sensorElevationAngle">elevation angle of the sensor in degrees</param>
    /// <param name="displayWidthMeters">Width of the display area in meters</param>
    /// <param name="displayHeightMeters">Height of the display area in meters</param>
    /// <param name="displayWidthPixels">Pixel width of the display area</param>
    /// <param name="displayHeightPixels">Pixel height of the display area</param>
    /// <returns>transform matrix</returns>
    public static Matrix4f sensorToScreenCoordinatesTransform(Vector3f sensorOffset, float sensorElevationAngle, float displayWidthMeters, float displayHeightMeters, float displayWidthPixels, float displayHeightPixels)
    {
        Matrix4f transform = sensorToScreenPositionTransform(sensorOffset, sensorElevationAngle);

        // For devices with square pixels, horizontalPixelsPerMeter and verticalPixelsPerMeter
        // should be equal.
        float horizontalPixelsPerMeter = displayWidthPixels / displayWidthMeters;
        float verticalPixelsPerMeter = displayHeightPixels / displayHeightMeters;
        
        transform = transform.scale(new Vector3f(horizontalPixelsPerMeter, -verticalPixelsPerMeter, 1.0f));
        transform = transform.translate(new Vector3f(displayWidthPixels / 2.0f, displayHeightPixels / 2.0f, 0.0f));

        
        return transform;
    }
	
	public static Vector3f transformSkeletonPoint(Matrix4f transform, Vector3f skeletonPoint) {
		Vector4f point = skeletonPoint.toVector4f();
		point = transform.multiply(point);
		return point.toVector3f();
	}
	
	public static Matrix4f getSensorToScreenPositionTransform() {
		return sensorToScreenPositionTransform(sensorOffsetFromScreenCenter, sensorElevationAngleToUse);
	}
	
	public static Matrix4f sensorToScreenPositionTransform(Vector3f sensorOffset, float sensorElevationAngle)
    {
        Matrix4f rotateIntoSensorSpace = new Matrix4f();
        rotateIntoSensorSpace = rotateIntoSensorSpace.rotate(sensorElevationAngle, 1.0f, 0.0f, 0.0f);

        Vector3f eye = sensorOffset.negate();
        eye = rotateIntoSensorSpace.multiply(eye.toVector4f()).toVector3f();

        Vector3f normalFromEye = new Vector3f(0.0f, 0.0f, 1.0f);
        normalFromEye = rotateIntoSensorSpace.multiply(normalFromEye.toVector4f()).toVector3f();

        Vector3f up = new Vector3f(0, 1, 0);

        return lookAt(eye, normalFromEye, up);
    }
	
	
	 public static Matrix4f lookAt(Vector3f eye, Vector3f lookDirection, Vector3f up)
     {
		 Vector3f n = lookDirection;
         n.normalize();
         
         Vector3f v = up.subtract(n.scale(up.dot(n)));
         v.normalize();

         // The "-" below makes this be a right-handed uvn space so we aren't
         // negating the x component in SensorToScreenCoordinatesTransform.
         // It also makes SensorToScreenPositionTransform give an immediately
         // usable transform without having to flip the X.
         Vector3f u = n.cross(v).negate();

         Matrix4f lookAt = new Matrix4f(
        		 new Vector4f(u.x, v.x, n.x, 0),
        		 new Vector4f(u.y, v.y, n.y, 0),
        		 new Vector4f(u.z, v.z, n.z, 0),
        		 new Vector4f(-u.dot(eye), -v.dot(eye), -n.dot(eye), 1)
        		 );

         return lookAt;
     }
}
