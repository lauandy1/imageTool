package com.tcl.imageTool.image;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.im4java.core.CompositeCmd;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.core.UFRawOperation;
import org.im4java.process.ArrayListOutputConsumer;
import org.im4java.process.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tcl.imageTool.exception.ImageProcessException;

public class ImageUtil {

	private static Logger logger = LoggerFactory.getLogger(ImageUtil.class);

	/**
	 * 获得图片尺寸信息
	 * @param imagePath
	 * @return
	 */
	public static ImageInfo getImageInfo(String imagePath) {
		logger.info("getImageInfo:imagePath={}", imagePath);
		ImageInfo imageInfo = new ImageInfo();
		String line = null;
		IMOperation op = new IMOperation();
		op.format("width:%w,height:%h,size:%b");
		op.addImage(1);
		IdentifyCmd identifyCmd = new IdentifyCmd(true);
		ArrayListOutputConsumer output = new ArrayListOutputConsumer();
		identifyCmd.setOutputConsumer(output);
		try {
			identifyCmd.run(op, imagePath);
		}
		catch (IOException e) {
			logger.error("", e);
			logger.info("getImageInfo err:imagePath={}", imagePath);
		}
		catch (InterruptedException e) {
			logger.error("", e);
			logger.info("getImageInfo err:imagePath={}", imagePath);
		}
		catch (IM4JavaException e) {
			logger.error("", e);
			logger.info("getImageInfo err:imagePath={}", imagePath);
		}
		ArrayList<String> cmdOutput = output.getOutput();
		line = cmdOutput.get(0);
		String[] ss = line.split(",");
		String s1 = ss[0];
		String s2 = ss[1];
		String s3 = ss[2];
		String[] ss1 = s1.split(":");
		String[] ss2 = s2.split(":");
		String[] ss3 = s3.split(":");
		int width = Integer.parseInt(ss1[1]);
		int height = Integer.parseInt(ss2[1]);
		String size = String.valueOf(ss3[1]);
		imageInfo.setWidth(width);
		imageInfo.setHeight(height);
		imageInfo.setSize(size);
		logger.info("getImageInfo:imageInfo={}", imageInfo);
		return imageInfo;
	}

	/**
	 * 从输入流获取图片信息
	 * @param is
	 * @return
	 */
	public static ImageInfo getImageInfo(InputStream is) {
		ImageInfo imageInfo = new ImageInfo();
		String line = null;
		IMOperation op = new IMOperation();
		op.format("width:%w,height:%h,size:%b");
		IdentifyCmd identifyCmd = new IdentifyCmd(true);
		ArrayListOutputConsumer output = new ArrayListOutputConsumer();
		identifyCmd.setOutputConsumer(output);

		op.addImage("-");
		Pipe pipeIn = new Pipe(is, null);

		identifyCmd.setInputProvider(pipeIn);

		try {
			identifyCmd.run(op);
		}
		catch (IOException e) {
			logger.error("", e);
			logger.info("getImageInfo err");
		}
		catch (InterruptedException e) {
			logger.error("", e);
			logger.info("getImageInfo err");
		}
		catch (IM4JavaException e) {
			logger.error("", e);
			logger.info("getImageInfo err");
		}
		ArrayList<String> cmdOutput = output.getOutput();
		line = cmdOutput.get(0);
		String[] ss = line.split(",");
		String s1 = ss[0];
		String s2 = ss[1];
		String s3 = ss[2];
		String[] ss1 = s1.split(":");
		String[] ss2 = s2.split(":");
		String[] ss3 = s3.split(":");
		int width = Integer.parseInt(ss1[1]);
		int height = Integer.parseInt(ss2[1]);
		String size = String.valueOf(ss3[1]);
		imageInfo.setWidth(width);
		imageInfo.setHeight(height);
		imageInfo.setSize(size);
		logger.info("getImageInfo:imageInfo={}", imageInfo);
		return imageInfo;
	}

	public static void cutImage(String srcPath, String newPath, int x, int y, int x1, int y1) {

		logger.info("cutImage:srcPath:{},newPath:{} ", srcPath, newPath);
		logger.info("cutImage:x:{},y:{},x1:{},y1:{}", new Object[] { x, y, x1, y1 });

		if (x >= x1 || y >= y1) {
			throw new ImageProcessException("End position must be after start position!");
		}
		int width = x1 - x;
		int height = y1 - y;
		IMOperation op = new IMOperation();
		op.addImage(srcPath);
		op.crop(width, height, x, y);
		op.addImage(newPath);

		ConvertCmd convert = new ConvertCmd(true);
		try {
			convert.run(op);
		}
		catch (IOException e) {
			logger.error("", e);
			logger.info("cutImage:srcPath:{},newPath:{} ", srcPath, newPath);
			logger.info("cutImage err:x:{},y:{},x1:{},y1:{}", new Object[] { x, y, x1, y1 });
		}
		catch (InterruptedException e) {
			logger.error("", e);
			logger.info("cutImage:srcPath:{},newPath:{} ", srcPath, newPath);
			logger.info("cutImage err:x:{},y:{},x1:{},y1:{}", new Object[] { x, y, x1, y1 });
		}
		catch (IM4JavaException e) {
			logger.error("", e);
			logger.info("cutImage:srcPath:{},newPath:{} ", srcPath, newPath);
			logger.info("cutImage err:x:{},y:{},x1:{},y1:{}", new Object[] { x, y, x1, y1 });
		}
	}

	public static byte[] cutImage(InputStream is, int x, int y, int x1, int y1) {
		logger.info("cutImage:x:{},y:{},x1:{},y1:{}", new Object[] { x, y, x1, y1 });
		byte[] bs = null;
		if (x >= x1 || y >= y1) {
			throw new ImageProcessException("End position must be after start position!");
		}
		int width = x1 - x;
		int height = y1 - y;
		ConvertCmd convert = new ConvertCmd(true);
		IMOperation op = new IMOperation();
		op.addImage("-");
		Pipe pipeIn = new Pipe(is, null);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Pipe pipeOut = new Pipe(null, os);
		op.crop(width, height, x, y);
		op.addImage("-");
		convert.setInputProvider(pipeIn);
		convert.setOutputConsumer(pipeOut);
		try {
			convert.run(op);
		}
		catch (IOException e) {
			logger.error("", e);
		}
		catch (InterruptedException e) {
			logger.error("", e);
		}
		catch (IM4JavaException e) {
			logger.error("", e);
			logger.info("cutImage err:x:{},y:{},x1:{},y1:{}", new Object[] { x, y, x1, y1 });
		}
		bs = os.toByteArray();
		try {
			os.close();
		}
		catch (IOException e) {
			logger.error("", e);
		}
		return bs;

	}

	/**
	 * 压缩图片质量
	 * @param width
	 * @param height
	 * @param srcPath
	 * @param newPath
	 * @param quality
	 */
	public static void compressImageQuality(String srcPath, String newPath, double quality) {
		IMOperation op = new IMOperation();
		op.addImage(srcPath);
		op.quality(quality);
		op.addImage(newPath);
		ConvertCmd convert = new ConvertCmd(true);
		try {
			convert.run(op);
		}
		catch (IOException e) {
			logger.error("", e);
			logger.info("zoomImage err:srcPath:{},newPath:{}", new Object[] { srcPath, newPath });
		}
		catch (InterruptedException e) {
			logger.error("", e);
			logger.info("zoomImage err:srcPath:{},newPath:{}", new Object[] { srcPath, newPath });
		}
		catch (IM4JavaException e) {
			logger.error("", e);
			logger.info("zoomImage err:srcPath:{},newPath:{}", new Object[] { srcPath, newPath });
		}
	}

	/**
	 * 压缩图片质量
	 * @param is
	 * @param quality
	 * @return
	 */
	public static byte[] compressImageQuality(InputStream is, double quality) {
		byte[] bs = null;
		IMOperation op = new IMOperation();
		op.addImage("-");
		op.quality(quality);
		Pipe pipeIn = new Pipe(is, null);

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		Pipe pipeOut = new Pipe(null, os);

		op.addImage("-");

		ConvertCmd convert = new ConvertCmd(true);

		convert.setInputProvider(pipeIn);
		convert.setOutputConsumer(pipeOut);
		try {
			convert.run(op);
		}
		catch (IOException e) {
			logger.error("", e);
		}
		catch (InterruptedException e) {
			logger.error("", e);
		}
		catch (IM4JavaException e) {
			logger.error("", e);
		}
		bs = os.toByteArray();
		try {
			os.close();
		}
		catch (IOException e) {
		}
		return bs;
	}

	public static void zoomImage(Integer width, Integer height, String srcPath, String newPath) {
		logger.info("zoomImage:width:{},height:{},srcPath:{},newPath:{}", new Object[] { width, height, srcPath,
				newPath });
		IMOperation op = new IMOperation();
		op.addImage(srcPath);
		op.quality(75.0);
		if (width == null) {// 根据高度缩放图片
			op.resize(null, height);
		}
		else if (height == null) {// 根据宽度缩放图片
			op.resize(width, null);
		}
		else {
			op.resize(width, height);
		}
		op.addImage(newPath);
		ConvertCmd convert = new ConvertCmd(true);
		try {
			convert.run(op);
		}
		catch (IOException e) {
			logger.error("", e);
			logger.info("zoomImage err:width:{},height:{},srcPath:{},newPath:{}", new Object[] { width, height,
					srcPath, newPath });
		}
		catch (InterruptedException e) {
			logger.error("", e);
			logger.info("zoomImage err:width:{},height:{},srcPath:{},newPath:{}", new Object[] { width, height,
					srcPath, newPath });
		}
		catch (IM4JavaException e) {
			logger.error("", e);
			logger.info("zoomImage err:width:{},height:{},srcPath:{},newPath:{}", new Object[] { width, height,
					srcPath, newPath });
		}
	}

	public static byte[] zoomImage(Integer width, Integer height, InputStream is) {
		logger.info("zoomImage:width:{},height:{}", width, height);
		byte[] bs = null;
		IMOperation op = new IMOperation();
		if (width == null) {// 根据高度缩放图片
			op.resize(null, height);
		}
		else if (height == null) {// 根据宽度缩放图片
			op.resize(width, null);
		}
		else {
			op.resize(width, height);
		}

		op.addImage("-");
		Pipe pipeIn = new Pipe(is, null);

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		Pipe pipeOut = new Pipe(null, os);

		op.addImage("-");

		ConvertCmd convert = new ConvertCmd(true);

		convert.setInputProvider(pipeIn);
		convert.setOutputConsumer(pipeOut);
		try {
			convert.run(op);
		}
		catch (IOException e) {
			logger.error("", e);
			logger.info("zoomImage err:width:{},height:{}", width, height);
		}
		catch (InterruptedException e) {
			logger.error("", e);
			logger.info("zoomImage err:width:{},height:{}", width, height);
		}
		catch (IM4JavaException e) {
			logger.error("", e);
			logger.info("zoomImage err:width:{},height:{}", width, height);
		}
		bs = os.toByteArray();
		try {
			os.close();
		}
		catch (IOException e) {
			logger.info("zoomImage:width:{},height:{}", width, height);
		}
		return bs;
	}

	/**
	 * 转换图片格式
	 * @param srcPath
	 * @param newPath
	 */
	public static byte[] convertImageFormat(InputStream is, String formatType) {

		byte[] bs = null;
		// IMOperation op = new IMOperation();

		// op.addImage("-");

		UFRawOperation op = new UFRawOperation();
		// op.outType(formatType);
		op.addImage("-");
		Pipe pipeIn = new Pipe(is, null);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Pipe pipeOut = new Pipe(null, os);
		op.addImage(formatType + ":-");

		ConvertCmd convert = new ConvertCmd(true);
		convert.setInputProvider(pipeIn);
		convert.setOutputConsumer(pipeOut);
		// MogrifyCmd mogrifyCmd = new MogrifyCmd(true);
		// mogrifyCmd.setInputProvider(pipeIn);
		// mogrifyCmd.setOutputConsumer(pipeOut);
		try {
			logger.info("op:{}", op);
			// mogrifyCmd.run(op);
			convert.run(op);
		}
		catch (IOException e) {
			logger.error("", e);
		}
		catch (InterruptedException e) {
			logger.error("", e);
		}
		catch (IM4JavaException e) {
			logger.error("", e);
		}
		bs = os.toByteArray();
		try {
			os.close();
		}
		catch (IOException e) {
			logger.error("", e);
		}
		return bs;

	}

	/**
	 * 按照给定的尺寸比例，先裁剪成相应的比例，然后再缩放
	 * @param is
	 * @param width
	 * @param height
	 * @return
	 */
	public static byte[] zoomAndCutImage(InputStream is, Integer width, Integer height) {

		// 需要多次读取is，所以先转换为字节数组
		byte[] orgin = null;
		try {
			orgin = readBytes(is);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 读取图片长宽，判断形状
		InputStream is1 = null;
		InputStream is2 = null;
		try {
			is1 = new ByteArrayInputStream(orgin);
			is2 = new ByteArrayInputStream(orgin);
			ImageInfo imageInfo = getImageInfo(is1);
			int orginWidth = imageInfo.getWidth();
			int orginHeight = imageInfo.getHeight();
			// 如果宽度为空，说明宽度不变
			if (width == null) {
				width = orginWidth;
			}
			else if (width < 0) {
				logger.info("width must be positive!");
				throw new IllegalArgumentException("width must be positive!");
			}
			if (height == null) {
				height = orginHeight;
			}
			else if (height < 0) {
				logger.info("height must be positive!");
				throw new IllegalArgumentException("height must be positive!");
			}
			// 计算指定尺寸的比例
			int ratio = orginWidth * height - width * orginHeight;
			// 如果大于0，说明原始图片比指定尺寸图片更加宽一些，所以要在宽度上进行裁剪
			if (ratio > 0) {
				int dstWidth = width * orginHeight / height;
				int tmp = (orginWidth - dstWidth) / 2;
				int x1 = tmp;
				int x2 = tmp + dstWidth;
				int y1 = 0;
				int y2 = orginHeight;
				byte[] res = cutImage(is2, x1, y1, x2, y2);
				InputStream isTmp = new ByteArrayInputStream(res);
				// 对裁剪后的图片进行缩放
				byte[] result = zoomImage(width, height, isTmp);
				// 关闭流
				isTmp.close();
				return result;
			}
			// 如果小于0，说明原始图片比指定尺寸图片更加高一些，所以要在高度上进行裁剪
			else if (ratio < 0) {
				int dstHeight = height * orginWidth / width;
				int tmp = (orginHeight - dstHeight) / 2;
				int x1 = 0;
				int x2 = orginWidth;
				int y1 = tmp;
				int y2 = tmp + dstHeight;
				byte[] res = cutImage(is2, x1, y1, x2, y2);
				InputStream isTmp = new ByteArrayInputStream(res);
				// 对裁剪后的图片进行缩放
				byte[] result = zoomImage(width, height, isTmp);
				// 关闭流
				isTmp.close();
				return result;
			}
			// 不需要裁剪，直接缩放
			else {
				// 对裁剪后的图片进行缩放
				byte[] result = zoomImage(width, height, is2);
				return result;
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		finally {
			try {
				is1.close();
				is2.close();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return orgin;

	}

	/**
	 * 将长方形图片裁剪为正方形图片，保留中间，截去两端
	 * @param is
	 * @return
	 */
	public static byte[] rectangularToSquare(InputStream is) {

		// 需要多次读取is，所以先转换为字节数组
		byte[] orgin = null;
		try {
			orgin = readBytes(is);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 读取图片长宽，判断形状
		InputStream is1 = null;
		InputStream is2 = null;
		try {
			is1 = new ByteArrayInputStream(orgin);
			is2 = new ByteArrayInputStream(orgin);

			ImageInfo imageInfo = getImageInfo(is1);
			int width = imageInfo.getWidth();
			int height = imageInfo.getHeight();
			// 如果是宽图片，保留中间，截去左右两端
			if (width > height) {
				// 计算截取坐标位置
				int tmp = (width - height) / 2;
				int x1 = tmp;
				int x2 = tmp + height;
				int y1 = 0;
				int y2 = height;
				return cutImage(is2, x1, y1, x2, y2);
			}
			else if (width == height) {
				return orgin;
			}
			// 如果是高图片，保留中间，截去上下两端
			else {
				// 计算截取坐标位置
				int tmp = (height - width) / 2;
				int x1 = 0;
				int x2 = width;
				int y1 = tmp;
				int y2 = tmp + width;
				return cutImage(is2, x1, y1, x2, y2);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		finally {
			try {
				is1.close();
				is2.close();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return orgin;

	}

	private static byte[] readBytes(InputStream in) throws IOException {
		BufferedInputStream bufin = new BufferedInputStream(in);
		int buffSize = 1024;
		ByteArrayOutputStream out = new ByteArrayOutputStream(buffSize);
		byte[] temp = new byte[buffSize];
		int size = 0;
		while ((size = bufin.read(temp)) != -1) {
			out.write(temp, 0, size);
		}
		byte[] content = out.toByteArray();
		return content;
	}

	public static byte[] compressImage(Integer width, Integer height, InputStream is) {
		logger.info("compressImage: width:{},height:{}", width, height);
		byte[] result = null;
		// 定义个字节数组保存原始字节流
		byte[] bs = null;
		try {
			bs = readBytes(is);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStream isOrgin = new ByteArrayInputStream(bs);
		ImageInfo imageInfo = getImageInfo(isOrgin);
		logger.info("compressImage: orgin image size:{}", imageInfo);
		int orgWidth = imageInfo.getWidth();
		int orgHeight = imageInfo.getHeight();
		// 返回原图
		if (orgWidth < width && orgHeight < height) {
			logger.info("compressImage: orgWidth:{},orgHeight:{}", orgWidth, orgHeight);
			// 返回原图
			result = bs;
		}
		// 压缩原图
		else {
			InputStream is1 = new ByteArrayInputStream(bs);
			result = zoomImage(width, height, is1);
		}
		return result;

	}

	public static void addImageText(String srcPath, String newPath, String position, int offsetH, int offsetV,
			String fontName, Integer fontSize, String fontColor, String content) {
		IMOperation op = new IMOperation();
		op.gravity(position);
		op.font(fontName);
		op.pointsize(fontSize);
		op.fill(fontColor);
		StringBuffer buf = new StringBuffer();
		buf.append("text ");
		buf.append(String.valueOf(offsetH));
		buf.append(",");
		buf.append(String.valueOf(offsetV));
		buf.append(" ");
		buf.append(content);
		op.draw(buf.toString());
		op.addImage(2);
		ConvertCmd convert = new ConvertCmd(true);
		try {
			convert.run(op, srcPath, newPath);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IM4JavaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 转换图片格式
	 * @param srcPath
	 * @param newPath
	 */
	public static void convertImageFormat(String srcPath, String newPath) {
		IMOperation op = new IMOperation();
		op.addImage(2);
		ConvertCmd convert = new ConvertCmd(true);
		try {
			convert.run(op, srcPath, newPath);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IM4JavaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void waterMark(String waterImagePath, String srcImagePath, String destImagePath, String gravity,
			int dissolve) {
		IMOperation op = new IMOperation();
		op.gravity(gravity);
		op.dissolve(dissolve);
		op.addImage(waterImagePath);
		op.addImage(srcImagePath);
		op.addImage(destImagePath);
		CompositeCmd cmd = new CompositeCmd(true);
		try {
			cmd.run(op);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (IM4JavaException e) {
			e.printStackTrace();
		}
	}

	// public static void hideWatermark(String srcPath, String newPath) {
	//
	// IMOperation op = new IMOperation();
	//
	// // op.size(100, 100, 35);
	// // op.stegano();
	// op.gravity("center");
	// op.region(100, 100);
	// op.negate();
	// op.addImage(srcPath);
	// op.addImage(newPath);
	// System.out.println(op);
	// ConvertCmd cmd = new ConvertCmd(true);
	// try {
	// cmd.run(op);
	// }
	// catch (IOException e) {
	// e.printStackTrace();
	// }
	// catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// catch (IM4JavaException e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	// public static void compressImage(int width, int height, String srcPath,
	// String dstPath) {
	// GMOperation op = new GMOperation();
	// op.addImage(srcPath);
	//
	// op.quality(75.0);
	//
	// op.addRawArgs("-resize", width + "x" + height);
	//
	// op.addRawArgs("-gravity", "center");
	// op.addImage(dstPath);
	//
	// ConvertCmd convert = new ConvertCmd(true);
	// // convert.setSearchPath("C:\\Program Files\\GraphicsMagick-1.3.18-Q8");
	// try {
	// convert.run(op);
	// }
	// catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// catch (IM4JavaException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static void main(String[] args) throws Exception {

		// convertImageFormat("C:\\Users\\lauandy\\Desktop\\test.jpg",
		// "C:\\Users\\lauandy\\Desktop\\test.webp");

		InputStream is = new FileInputStream("C:\\Users\\lauandy\\Desktop\\test.jpg");
		byte[] bs = convertImageFormat(is, "webp");
		FileOutputStream fos = new FileOutputStream("C:\\Users\\lauandy\\Desktop\\test_after.webp");
		fos.write(bs);
		fos.flush();
		is.close();
		fos.close();

		// FileInputStream is = new
		// FileInputStream("C:\\Users\\lauandy\\Desktop\\test_after.webp");
		// byte[] b = new byte[3];
		// is.read(b, 0, b.length);
		// System.out.println(bytesToHexString(b));

		// cutImage("C:\\Users\\lauandy\\Desktop\\pic\\Tulips.jpg",
		// "C:\\Users\\lauandy\\Desktop\\pic\\Tulips_720x100.jpg", 0, 200, 720,
		// 300);
		// cutImage("C:\\Users\\lauandy\\Desktop\\pic\\Tulips.jpg",
		// "C:\\Users\\lauandy\\Desktop\\pic\\Tulips_720x150.jpg", 0, 200, 720,
		// 350);
		// cutImage("C:\\Users\\lauandy\\Desktop\\pic\\Tulips.jpg",
		// "C:\\Users\\lauandy\\Desktop\\pic\\Tulips_720x200.jpg", 0, 200, 720,
		// 400);

		// compressImageQuality("C:\\Users\\lauandy\\Desktop\\pic\\Tulips.jpg",
		// "C:\\Users\\lauandy\\Desktop\\pic\\Tulips_small.jpg", 10);

		// InputStream is = new
		// FileInputStream("C:\\Users\\lauandy\\Desktop\\pic\\Tulips.jpg");
		// byte[] bs = compressImageQuality(is, 30);
		// FileOutputStream fos = new
		// FileOutputStream("C:\\Users\\lauandy\\Desktop\\pic\\Tulips_small.jpg");
		// fos.write(bs);
		// fos.flush();
		// is.close();
		// fos.close();

		// InputStream is = new
		// FileInputStream("C:\\Users\\lauandy\\Desktop\\a.jpg");
		// byte[] bs = rectangularToSquare(is);
		// InputStream is1 = new ByteArrayInputStream(bs);
		// // byte[] bs = zoomAndCutImage(is, 100, 100);
		// byte[] bs1 = zoomImage(100, 100, is1);
		// FileOutputStream fos = new
		// FileOutputStream("C:\\Users\\lauandy\\Desktop\\a2.jpg");
		// fos.write(bs1);
		// fos.flush();
		// is.close();
		// fos.close();

		// zoomImage(720, 720,
		// "C:\\Users\\lauandy\\Desktop\\pic\\Tulips.jpg",
		// "C:\\Users\\lauandy\\Desktop\\pic\\Tulips_720x720.jpg");
		// zoomImage(720, 150,
		// "C:\\Users\\lauandy\\Desktop\\pic\\Tulips.jpg",
		// "C:\\Users\\lauandy\\Desktop\\pic\\Tulips_720x150.jpg");
		// zoomImage(720, 200,
		// "C:\\Users\\lauandy\\Desktop\\pic\\Tulips.jpg",
		// "C:\\Users\\lauandy\\Desktop\\pic\\Tulips_720x200.jpg");

		// System.out.println(getImageInfo("D:\\001.jpg"));
	}
}
