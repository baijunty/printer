package com.baijunty.printer.jolimark.enums;

/**
 * 文档类型
 * 
 * @author Jolimark
 *
 */
public enum FileTypeEnum {
	PDF(1), Image(2);

	/**
	 * 文件类型
	 */
	private int type;

	private FileTypeEnum(int t) {
		this.setType(t);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
