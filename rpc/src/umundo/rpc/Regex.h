/**
 *  @file
 *  @brief      Regular Expressions with PCRE.
 *  @author     2012 Stefan Radomski (stefan.radomski@cs.tu-darmstadt.de)
 *  @copyright  Simplified BSD
 *
 *  @cond
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the FreeBSD license as published by the FreeBSD
 *  project.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the FreeBSD license along with this
 *  program. If not, see <http://www.opensource.org/licenses/bsd-license>.
 *  @endcond
 */

#ifndef REGEX_H_HUE92M
#define REGEX_H_HUE92M

#include <umundo/common/Common.h>
#include <string>
#include <vector>

#define OVECCOUNT 30    /* should be a multiple of 3 */

namespace umundo {

/**
 * Match a regular expression against a string.
 *
 * Matches are expressed as pairs of offset and length.
 */
class UMUNDO_API Regex {
public:
	Regex(const std::string&);
	~Regex();

	bool hasError();
	bool matches(const std::string&);
	void setPattern(const std::string&);

	const std::string getPattern()                         {
		return _pattern;
	}
	bool hasSubMatches()                              {
		return _subMatchIndex.size() > 0;
	}

	std::string getMatch();
	std::vector<std::string> getSubMatches();

protected:
	void* _re;
	std::string _pattern;
	int _ovector[OVECCOUNT];
	std::pair<int, int> _matchIndex;
	std::vector<std::pair<int, int> > _subMatchIndex;

	std::string _text;
	std::string _error;
	int _errorOffset;
};

}

#endif /* end of include guard: REGEX_H_HUE92M */
