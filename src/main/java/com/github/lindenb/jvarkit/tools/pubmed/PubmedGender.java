/*
The MIT License (MIT)

Copyright (c) 2014 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


History:
* 2014 creation

*/
package com.github.lindenb.jvarkit.tools.pubmed;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import com.github.lindenb.jvarkit.io.IOUtils;

import htsjdk.samtools.util.CloserUtil;

/**
 * PubmedGender
 *
 */
public class PubmedGender
	extends AbstractPubmedGender
	{
	private static final org.slf4j.Logger LOG = com.github.lindenb.jvarkit.util.log.Logging.getLog(PubmedGender.class);

	private static class GenderInfo {
		String male=null;
		String female=null;
	}
	private final Map<String, GenderInfo> name2gender;

	public PubmedGender()
		{
		final Collator collator= Collator.getInstance(Locale.US);
		collator.setStrength(Collator.PRIMARY);
		this.name2gender=new TreeMap<String, GenderInfo>(collator);
		}
	
	@Override
	protected Collection<Throwable> call(String inputName) throws Exception {
		if(super.dataFile==null || !super.dataFile.exists()) {
			return wrapException("Undefined option -"+OPTION_DATAFILE);
		}
		OutputStream out=null;
		XMLEventReader r=null;
		InputStream in=null;
		XMLEventWriter w=null;
		final QName maleName =new QName("male");
		final QName femaleName =new QName("female");
		BufferedReader br=null;
		try {
			final Pattern comma=Pattern.compile("[,]");
			LOG.info("load "+super.dataFile);
			this.name2gender.clear();
			br = IOUtils.openFileForBufferedReading(super.dataFile);
			String line;
			while((line=br.readLine())!=null) {
				if(line.startsWith("#")) continue;
				final String tokens[]= comma.split(line);
				if(tokens.length!=3) return wrapException("expected 3 comma-separated columns in "+line);
				tokens[0]=tokens[0].toLowerCase();
				GenderInfo gi = this.name2gender.get(tokens[0]);
				if(gi==null) {
					gi= new GenderInfo();
					this.name2gender.put(tokens[0],gi);
				}
				if(tokens[1].equals("F")) {
					gi.female=tokens[2];
				} else if(tokens[1].equals("M")) {
					gi.male=tokens[2];
				} else
				{
					return wrapException("expected 'M' or 'F' in 2nd column  in "+line);
				}
				
			}
			br.close();
			LOG.info("database contains "+this.name2gender.size());
			
			
			final XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
			final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
			xmlInputFactory.setXMLResolver(new XMLResolver() {
				@Override
				public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace)
						throws XMLStreamException {
					LOG.debug("Ignoring resolve Entity");
					return new ByteArrayInputStream(new byte[0]);
				}
			});
			in=(inputName==null?stdin():IOUtils.openURIForReading(inputName));
			r = xmlInputFactory.createXMLEventReader(in);
			
			out = super.openFileOrStdoutAsStream();
			final XMLOutputFactory xof = XMLOutputFactory.newFactory();
			w=xof.createXMLEventWriter(out, "UTF-8");
			while(r.hasNext()) {
			final XMLEvent evt = r.nextEvent();
			if(evt.isStartElement() &&
					evt.asStartElement().getName().getLocalPart().equals("Author"))
				{
				String initials=null;
				String firstName =null;
				final List<XMLEvent> events = new ArrayList<>();
				while(r.hasNext()) {
					final XMLEvent evt2 = r.nextEvent();
					events.add(evt2);
					if(evt2.isStartElement()) {
						final String eltName = evt2.asStartElement().getName().getLocalPart();
						if((eltName.equals("ForeName") || eltName.equals("FirstName")) && r.peek().isCharacters()) {
							final XMLEvent textEvt = r.nextEvent();
							events.add(textEvt);
							firstName= textEvt.asCharacters().getData();	
							}
						else if(eltName.equals("Initials") && r.peek().isCharacters()) {
							final XMLEvent textEvt = r.nextEvent();
							events.add(textEvt);
							initials= textEvt.asCharacters().getData();	
							}
						}
					else if(evt2.isEndElement() && evt2.asEndElement().getName().getLocalPart().equals("Author")) {
						break;
					} 
				}
				
				
					
				if( firstName!=null) {
					final String tokens[]=firstName.toLowerCase().split("[ ,]+");
					firstName="";
					for(final String s:tokens)
						{
						if(s.length()> firstName.length())
							{
							firstName=s;
							}
						}
					
					if(	firstName.length()==1 ||
						(initials!=null && firstName.equals(initials.toLowerCase())))
						{
						firstName=null;
						}
					}
				
				String female=null;
				String male=null;

				if(firstName!=null) {
					final GenderInfo gi = this.name2gender.get(firstName);
					if(gi!=null) {
						male=gi.male;
						female=gi.female;
						}
					}
				
				
				final List<Attribute> attributes = new ArrayList<>();
				Iterator<?> t=evt.asStartElement().getAttributes();
				while(t.hasNext()) {
				    final Attribute att =  (Attribute)t.next();
				    if(att.getName().equals(maleName)) continue;
				    if(att.getName().equals(femaleName)) continue;
					attributes.add(att);
				}
				
				if(male!=null) attributes.add(xmlEventFactory.createAttribute(maleName, male));
				if(female!=null) attributes.add(xmlEventFactory.createAttribute(femaleName, female));
				
				w.add(xmlEventFactory.createStartElement(
						evt.asStartElement().getName(),
						attributes.iterator(),
						evt.asStartElement().getNamespaces()
						));
				
				
				
				for(final XMLEvent evt2:events) w.add(evt2);
				continue;
				}
			w.add(evt);
			}
			
			r.close();r=null;
			in.close();in=null;
			w.flush();w.close();w=null;
			out.flush();out.close();out=null;
			return RETURN_OK;
		} catch (Exception e) {
			return wrapException(e);
		} finally {
			CloserUtil.close(r);
			CloserUtil.close(in);
			CloserUtil.close(w);
			CloserUtil.close(out);
			CloserUtil.close(br);
			this.name2gender.clear();
		}

		}
		
	
	public static void main(String[] args) {
		new PubmedGender().instanceMainWithExit(args);
	}
}