package com.crawljax.stateabstractions.visual;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.resize;

import java.io.IOException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class SSIM {

    public static Scalar getMSSIM(Mat i1, Mat i2) {
        double C1 = 6.5025, C2 = 58.5225;
        /***************************** INITS **********************************/
        int d = CV_32F;
        if (i1.size().height != i2.size().height || i1.size().width != i2.size().width) {
            Mat newMat = new Mat(
                    new Size(Math.max(i1.size().width, i2.size().width), Math.max(i1.size().height, i2.size().height)),
                    i1.type());
            // System.out.println(newMat);
            resize(i1, i1, newMat.size());
            resize(i2, i2, newMat.size());
        }

        Mat I1 = new Mat();
        Mat I2 = new Mat();
        i1.convertTo(I1, d); // cannot calculate on one byte large values
        i2.convertTo(I2, d);

        Mat I2_2 = I2.mul(I2); // I2^2
        Mat I1_2 = I1.mul(I1); // I1^2
        Mat I1_I2 = I1.mul(I2); // I1 * I2

        /*************************** END INITS **********************************/
        Mat mu1 = new Mat();
        Mat mu2 = new Mat(); // PRELIMINARY COMPUTING
        GaussianBlur(I1, mu1, new Size(11, 11), 1.5);
        GaussianBlur(I2, mu2, new Size(11, 11), 1.5);
        Mat mu1_2 = mu1.mul(mu1);
        Mat mu2_2 = mu2.mul(mu2);
        Mat mu1_mu2 = mu1.mul(mu2);

        //		displayMat(mu1_mu2);
        Mat sigma1_2 = new Mat();
        Mat sigma2_2 = new Mat();
        Mat sigma12 = new Mat();

        GaussianBlur(I1_2, sigma1_2, new Size(11, 11), 1.5);
        // sigma1_2 -= mu1_2;
        Core.subtract(sigma1_2, mu1_2, sigma1_2);
        //		displayMat(sigma1_2);
        GaussianBlur(I2_2, sigma2_2, new Size(11, 11), 1.5);
        // sigma2_2 -= mu2_2;
        Core.subtract(sigma2_2, mu2_2, sigma2_2);
        //		displayMat(sigma2_2);
        GaussianBlur(I1_I2, sigma12, new Size(11, 11), 1.5);
        // sigma12 -= mu1_mu2;
        Core.subtract(sigma12, mu1_mu2, sigma12);
        Mat t1 = new Mat(), t2 = new Mat(), t3 = new Mat();

        Scalar scalar = new Scalar(C1);
        Core.multiply(mu1_mu2, new Scalar(2), t1);
        Core.add(t1, scalar, t1);
        Core.multiply(sigma12, new Scalar(2), t2);
        Core.add(t2, new Scalar(C2), t2);
        t3 = t1.mul(t2); // t3 = ((2*mu1_mu2 + C1).*(2*sigma12 + C2))
        Core.add(mu1_2, mu2_2, t1);
        Core.add(t1, new Scalar(C1), t1);
        Core.add(sigma1_2, sigma2_2, t2);
        Core.add(t2, new Scalar(C2), t2);
        t1 = t1.mul(t2); // t1 =((mu1_2 + mu2_2 + C1).*(sigma1_2 + sigma2_2 + C2))
        //		displayMat(t1);

        Mat ssim_map = new Mat();
        Core.divide(t3, t1, ssim_map); // ssim_map =  t3./t1;
        Scalar mssim = Core.mean(ssim_map); // mssim = average of ssim map
        return mssim;
    }

    public static void main(String[] args) throws IOException {

        Mat image1 = Imgcodecs.imread("image_difference_input.png");

        Mat image2 = Imgcodecs.imread("image_difference_input2.png");

        long start = System.currentTimeMillis();
        start = System.currentTimeMillis();
        Imgproc.cvtColor(image1, image1, COLOR_RGB2GRAY);
        Imgproc.cvtColor(image2, image2, COLOR_RGB2GRAY);

        Scalar scalar = getMSSIM(image1, image2);
        long ssimTime = System.currentTimeMillis() - start;
        // System.out.println("PSNR result is " + PSNR_Result + " time used " + psnrTime);
        System.out.println("SSIM result is " + scalar + " time used " + ssimTime);
    }
}
