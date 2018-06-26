/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.bhm.sdk.bhmlibrary.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileHelper {

	private static final String _DATA = "_data";

	/**
	 * Returns the real path of the given URI string. If the given URI string
	 * represents a content:// URI, the real path is retrieved from the media
	 * store.
	 *
	 * @param uriString
	 *            the URI string of the audio/image/video
	 *            the current application context
	 * @return the full path to the file
	 */
	@SuppressWarnings("deprecation")
	public static String getRealPath(String uriString, Activity ctx) {
		String realPath = null;

		if (uriString.startsWith("content://")) {
			String[] proj = { _DATA };
			Cursor cursor = ctx.managedQuery(Uri.parse(uriString), proj, null,
					null, null);
			if (null != cursor) {

				int column_index = cursor.getColumnIndexOrThrow(_DATA);
				cursor.moveToFirst();
				realPath = cursor.getString(column_index);
			}
			if (realPath == null) {
				// LOG.e(LOG_TAG, "Could get real path for URI string %s",
				// uriString);
			}
		} else if (uriString.startsWith("file://")) {
			realPath = uriString.substring(7);
			if (realPath.startsWith("/android_asset/")) {
				realPath = null;
			}
		} else {
			realPath = uriString;
		}

		return realPath;
	}

	/**
	 * Returns the real path of the given URI. If the given URI is a content://
	 * URI, the real path is retrieved from the media store.
	 *
	 * @param uri
	 *            the URI of the audio/image/video
	 *            the current application context
	 * @return the full path to the file
	 */
	public static String getRealPath(Uri uri, Activity ctx) {
		return FileHelper.getRealPath(uri.toString(), ctx);
	}

	/**
	 * Returns an input stream based on given URI string.
	 *
	 * @param uriString
	 *            the URI string from which to obtain the input stream
	 *            the current application context
	 * @return an input stream into the data at the given URI or null if given
	 *         an invalid URI string
	 * @throws IOException
	 */
	public static InputStream getInputStreamFromUriString(String uriString,
														  Activity ctx) throws IOException {
		if (uriString.startsWith("content")) {
			Uri uri = Uri.parse(uriString);
			return ctx.getContentResolver().openInputStream(uri);
		} else if (uriString.startsWith("file:///android_asset/")) {
			Uri uri = Uri.parse(uriString);
			String relativePath = uri.getPath().substring(15);
			/*String relateivPathArags[]=relativePath.split("/");
			
			String s1=relateivPathArags[0];
			String s2=relateivPathArags[1];
			
			String relativePath1=s1+"/page"+"/"+s2;*/

			return ctx.getAssets().open(relativePath);
		} else {
			return new FileInputStream(getRealPath(uriString, ctx));
		}
	}

	/**
	 * Removes the "file://" prefix from the given URI string, if applicable. If
	 * the given URI string doesn't have a "file://" prefix, it is returned
	 * unchanged.
	 *
	 * @param uriString
	 *            the URI string to operate on
	 * @return a path without the "file://" prefix
	 */
	public static String stripFileProtocol(String uriString) {
		if (uriString.startsWith("file://")) {
			uriString = uriString.substring(7);
		}
		return uriString;
	}

	/**
	 * Returns the mime type of the data specified by the given URI string.
	 *
	 * @param uriString
	 *            the URI string of the data
	 * @return the mime type of the specified data
	 */
	@SuppressLint("DefaultLocale")
	public static String getMimeType(String uriString, Activity ctx) {
		String mimeType = null;

		Uri uri = Uri.parse(uriString);
		if (uriString.startsWith("content://")) {
			mimeType = ctx.getContentResolver().getType(uri);
		} else {
			// MimeTypeMap.getFileExtensionFromUrl() fails when there are query
			// parameters.
			String extension = uri.getPath();
			int lastDot = extension.lastIndexOf('.');
			if (lastDot != -1) {
				extension = extension.substring(lastDot + 1);
			}
			// Convert the URI string to lower case to ensure compatibility with
			// MimeTypeMap (see CB-2185).
			extension = extension.toLowerCase();
			if ("3ga".equals(extension)) {
				mimeType = "audio/3gpp";
			} else {
				mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
						extension);
			}
		}
		return mimeType;
	}
}