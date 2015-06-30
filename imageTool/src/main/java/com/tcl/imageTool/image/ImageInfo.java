package com.tcl.imageTool.image;

public class ImageInfo {

	private int width;// 图片宽度

	private int height;// 图片高度

	private String size;// 图片大小

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public String toString() {
		return "ImageInfo [width=" + width + ", height=" + height + ", size=" + size + "]";
	}

}
